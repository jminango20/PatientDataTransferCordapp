package com.template.model

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class PatientData(val idPaciente: Int,
                       val nomePaciente: String,
                       //val dataInicio: Instant, //Java representa un instante no tempo: Data, Hora, Minuto, Segundo, Milisegundo
                       val idade: Int,
                       val centroSaude: Party //No dentro da rede
                       )
