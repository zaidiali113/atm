package com.example.atm.model

import java.time.LocalDateTime

data class TransactionEvent(
    val atmId: String,
    val customerId: String,
    val timestamp: LocalDateTime
)