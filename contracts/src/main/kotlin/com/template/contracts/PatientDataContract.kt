package com.template.contracts

import com.template.states.PatientDataState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import java.lang.IllegalStateException

class PatientDataContract : Contract {
    override fun verify(tx: LedgerTransaction) {

        val comando = tx.commandsOfType<Commands>().single()
        when(comando.value){
            is Commands.GerarPatientData -> verifyGerarPatientData(tx)
            is Commands.EnviarPatientData -> verifyEnviarPatientData(tx)
            else -> throw IllegalStateException("Comando nao reconhecido")
        }
    }


    //FUNCOES DE VERIFICACAO
    fun verifyGerarPatientData(tx: LedgerTransaction){
        requireThat {
            "Tem que ter um e apenas um Output." using (tx.outputsOfType<PatientDataState>().size == 1)
            "Nao deve haver Input." using (tx.inputsOfType<PatientDataState>().isEmpty())

            val outputs =  tx.outputsOfType<PatientDataState>()
            //Regras geracao do PatientData
            "É necessario que tenha um id de paciente" using outputs.all { it.patientData.idPaciente > 0 }
        }
    }

    fun verifyEnviarPatientData(tx: LedgerTransaction){
        requireThat {
            "Tem que ter um e apenas um Input." using (tx.inputsOfType<PatientDataState>().size==1)
            "Tem que ter um e apenas um Output." using (tx.outputsOfType<PatientDataState>().size==1)

            val inputs = tx.inputsOfType<PatientDataState>().single()
            val outputs =  tx.outputsOfType<PatientDataState>().single()
            //Regras geracao do PatientData
            "O PatientData nao pode ser alterada" using (outputs.patientData == inputs.patientData)
        }
    }


    //COMANDOS
    interface Commands: CommandData {
        class GerarPatientData : Commands
        class EnviarPatientData : Commands
    }


}