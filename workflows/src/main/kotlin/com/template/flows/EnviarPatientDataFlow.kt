package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.PatientDataContract
import com.template.states.PatientDataState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

object EnviarPatientDataFlow {

    //Recebemos o ID do PatientData (LinearState) que queremos enviar, e o outro no, centroSaude, que irá receber o PatientData.
    @InitiatingFlow
    @StartableByRPC
    class ReqFlow(val historicoId: UUID, val para: Party): FlowLogic<SignedTransaction>(){
        @Suspendable
        override fun call(): SignedTransaction {

            //1) Consultar na DB o State atual do PatientData Histórico. Para isso, utilizamos o serviço de “Vault”,
            // que irá manter o estado final.
            //Queremos consultar um PatientData que tenha o linearId igual ao que foi recebido por parametro.
            //Para isto, utilizamos o critério de query “QueryCriteria.LinearStateQueryCriteria”, que aceita uma lista
            //de LinearIds e retorna todos na lista “states”. Como estamos pesquisando apenas por um ID é seguro chamar
            // o método “single()” e pegar apenas um valor da lista.
            //-----------------------
            //O objeto retornado não é apenas o State, ele também contém um outro valor que é chamado de Ref.
            //O Ref é a referência deste State específico no seu estado atual. Com ele, é possível encontrar o State no
            //DB, e a partir dele, é possível encontrar todos os estados anteriores pelos quais este State já passou.
            val historicoStateAndRef = serviceHub.vaultService.queryBy<PatientDataState>(
                QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier(id = historicoId)))).states.single()
            //Por enquanto so precisamos do State
            val historicoState = historicoStateAndRef.state.data

            //2) Validar para ter certeza de que não estamos transmitindo informação de outro centroSaude.
            requireThat {
                "Eu tenho que ser o centroSaude emissor do patientData para enviar para outro centroSaude" using (historicoState.patientData.centroSaude == ourIdentity)
            }

            //3) Precisamos utilizar o mesmo notary que foi utilizado anteriormente para nao ter conflitos
            val notary = historicoStateAndRef.state.notary

            //4) Criar o novo estado do State
            //Como o nosso State é um “data class”, temos o método copy, que permite criar uma cópia do objeto alterando
            // apenas as informações necessárias.
            val novoHistoricoState = historicoState.copy(centroSaudeReceptoras = historicoState.centroSaudeReceptoras + para)

            //5) Criamos o comando
            val comando = Command(PatientDataContract.Commands.EnviarPatientData(), novoHistoricoState.participants.map { it.owningKey })

            //6) Criamos a transacao
            //Desta vez especificamos uma Entrada e uma Saída, para indicar que estamos fazendo uma atualização do State
            val txBuilder = TransactionBuilder(notary)
                .addInputState(historicoStateAndRef)
                .addOutputState(novoHistoricoState, PatientDataContract::class.java.canonicalName)
                .addCommand(comando)

            //7) Verificamos o contrato
            txBuilder.verify(serviceHub)

            //8) Assinamos a transacao, que agora esta validada
            val transacaoParcialAssinada = serviceHub.signInitialTransaction(txBuilder)

            //9) Desta vez, precisamos que mais nos assinem a transação. Precisamos ter certeza de que a alteração que
            // estamos fazendo é válida em todos os nós, e garantir que eles não tem nenhuma crítica à transação.
            //Abrir uma sessa de comunicacao com cada uma das demais faculdades
            val listaSessao = novoHistoricoState.centroSaudeReceptoras.map { initiateFlow(it) }
            //A função map é uma função de lista, que aplica para todos os seus elementos a função que foi passada para
            // ele, e no final gera uma nova lista com todos os resultados retornados.

            //10) Agora que temos as sessões, conseguimos comunicar de forma segura com os outros nós.
            // Como a coleta de assinatura é um processo padrão, já existe um Flow no Corda que cuida disso,
            // o “CollectSignaturesFlow”, que irá enviar para todos os nós tomarem uma decisão sobre o contrato e então
            // devolver a transação com a sua assinatura.
            val transacaoTotalmenteAssinada = subFlow(CollectSignaturesFlow(transacaoParcialAssinada, listaSessao))

            //11) Agora que a transação tem todas as assinaturas necessárias, chamamos o “FinalityFlow” para finalizar a transação.
            return subFlow(FinalityFlow(transacaoTotalmenteAssinada))

            //-------------------------------------------
            //Com isso descrevemos tudo que irá acontecer do lado do node que enviar as transações

        }
    }
    //-------------------------------------------
    //Descrever as ações que serão tomadas pelo nó receptor da transação.
    //Precisa receber como parâmetro a sessão que foi criada com o node que está enviado a transação.
    @InitiatedBy(ReqFlow::class)
    class RespFlow(val session: FlowSession) : FlowLogic<SignedTransaction>(){
        @Suspendable
        override fun call(): SignedTransaction {
            //Responder à chamada de coleta de assinatura, com o “SignTransactionFlow”
            val signTransactionFlow = object : SignTransactionFlow(session){
                override fun checkTransaction(stx: SignedTransaction) = requireThat{
                    val outputs = stx.coreTransaction.outputsOfType<PatientDataState>()
                    "Tinha que ter recebido um dataPatient historico !" using (outputs.isNotEmpty())
                    "O state nao pode ser emitido no meu nome." using outputs.all { it.patientData.centroSaude != ourIdentity }
                }
                //Foi garantido que ninguém está mandando informação nossa, podemos executar este flow e retornar a chamada.
            }
            return subFlow(signTransactionFlow)
        }
    }
}