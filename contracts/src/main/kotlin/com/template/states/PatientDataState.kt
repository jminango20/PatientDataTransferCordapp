package com.template.states

import com.template.contracts.PatientDataContract
import com.template.model.PatientData
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@CordaSerializable
@BelongsToContract(PatientDataContract::class)
data class PatientDataState(
    val patientData: PatientData,
    val dataEnvio: List<Instant> = listOf(), //Java representa un instante no tempo: Data, Hora, Minuto, Segundo, Milisegundo
    val centroSaudeReceptoras: List<Party> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    //val observacao : String ? = "Lido o PatientData",
    //val observacoes : List<String> ? = null
    ) : LinearState {
        override val participants: List<AbstractParty>
        get() = centroSaudeReceptoras + patientData.centroSaudeOrigem
    }
