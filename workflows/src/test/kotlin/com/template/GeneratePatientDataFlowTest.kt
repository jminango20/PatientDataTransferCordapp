package com.template

import com.template.flows.SaveDataPatientFlow
import com.template.model.PatientData
import com.template.states.PatientDataState
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GeneratePatientDataFlowTest  {
    lateinit var network: MockNetwork
    lateinit var ijc : StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
            ))
        )
        ijc = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }


    @Test
    fun `deve gerar patient data`() {
        val patientData = PatientData(idPaciente = 1,
                                      nomePaciente = "Juan",
                                      idade = 33,
                                      centroSaudeOrigem = ijc.info.legalIdentities.first()
                                     )

        val flow = SaveDataPatientFlow.ReqFlow(patientData)
        val future = ijc.startFlow(flow)
        network.runNetwork()

        val signedTransaction = future.get()
        val output = signedTransaction.coreTransaction.outputsOfType<PatientDataState>().single()
        assertEquals(output.patientData, patientData)
    }

}