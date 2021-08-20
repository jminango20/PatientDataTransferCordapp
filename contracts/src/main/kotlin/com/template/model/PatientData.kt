package com.template.model

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@CordaSerializable
data class PatientData(val idPaciente: Int,
                       val nomePaciente: String,
                       val dataCriacao: Instant = Instant.now(),
                       val idade: Int,
                       val centroSaudeOrigem: Party //No dentro da rede
                       )
