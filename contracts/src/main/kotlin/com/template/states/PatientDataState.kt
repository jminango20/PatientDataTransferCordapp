package com.template.states

import com.template.contracts.PatientDataContract
import com.template.model.PatientData
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@BelongsToContract(PatientDataContract::class)
data class PatientDataState(
    val patientData: PatientData,
    val centroSaudeReceptoras: List<Party> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    override val participants: List<AbstractParty>
        get() = centroSaudeReceptoras + patientData.centroSaude
}
