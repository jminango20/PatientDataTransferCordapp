package com.template

import com.template.flows.EnviarPatientDataFlow
import com.template.flows.SaveDataPatientFlow
import com.template.model.PatientData
import com.template.states.PatientDataState
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnviarPatientDataFlowTest  {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var patientDataState: PatientDataState

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
            ))
        )
        a = network.createPartyNode()
        b = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        //listOf(a, b).forEach { it.registerInitiatedFlow(EnviarHistoricoFlow.RespFlow::class.java) }
        val startedNodes = arrayListOf(a,b)
        startedNodes.forEach { it.registerInitiatedFlow(EnviarPatientDataFlow.RespFlow::class.java) }
        network.runNetwork()
        patientDataState = gerarPatientData()
    }


    fun gerarPatientData() : PatientDataState {
        val disciplina = PatientData(
            idPaciente = 1,
            nomePaciente = "Juan",
            //val dataInicio: Instant, //Java representa un instante no tempo: Data, Hora, Minuto, Segundo, Milisegundo
            idade = 33,
            centroSaude = a.info.legalIdentities.first()//No dentro da rede
            )

        val flow = SaveDataPatientFlow.ReqFlow(disciplina)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.get()
        val output = signedTransaction.coreTransaction.outputsOfType<PatientDataState>().single()

        return output
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `deve enviar patient data`() {

        val flow = EnviarPatientDataFlow.ReqFlow(patientDataState.linearId.id, b.info.legalIdentities.first())
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.get()
        val outputState = signedTransaction.coreTransaction.outputsOfType<PatientDataState>().single()
        assertEquals(outputState.patientData, patientDataState.patientData)
        assertTrue(outputState.centroSaudeReceptoras.containsAll(patientDataState.centroSaudeReceptoras))
        assertTrue((patientDataState.centroSaudeReceptoras.size + 1) == outputState.centroSaudeReceptoras.size)

        listOf(a, b).forEach {
            val vaultState = it.services.vaultService.queryBy<PatientDataState>(
                QueryCriteria.LinearStateQueryCriteria(
                    linearId = listOf(patientDataState.linearId))).states.single().state.data
            assertEquals(vaultState, outputState)
        }
    }


}