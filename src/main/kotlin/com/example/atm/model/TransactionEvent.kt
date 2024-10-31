package com.example.atm.model

import java.time.LocalDateTime

enum class TransactionType {
    DEPOSIT,
    CASH_WITHDRAWAL,
    BALANCE_INFORMATION_REQUEST
}

data class TransactionEvent(
    val atmId: String,
    val customerId: String,
    val timestamp: LocalDateTime,
    val transactionType: TransactionType
)