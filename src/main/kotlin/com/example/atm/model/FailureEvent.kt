package com.example.atm.model

import java.time.Duration
import java.time.LocalDateTime

enum class FailureType {
    HARDWARE_FAILURE,
    NETWORK_FAILURE,
    SOFTWARE_FAILURE,
    UNKNOWN
}

data class FailureEvent(
    val atmId: String,
    val timestamp: LocalDateTime,
    val failureType: FailureType,
    val transactionType: TransactionType?, // Can be null if not associated with a transaction
    val incidentContext: String,           // Additional context or error message
    val downtime: Duration
)

