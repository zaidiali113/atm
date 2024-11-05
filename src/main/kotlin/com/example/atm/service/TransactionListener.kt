package com.example.atm.service

import com.example.atm.model.TransactionEvent
import com.example.atm.model.TransactionType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class TransactionListener {

    // In-memory data structure to store transaction events
    private val transactionMap = ConcurrentHashMap<String, MutableList<TransactionEvent>>()

    @KafkaListener(topics = ["transaction_logs"], groupId = "atm-monitoring-group")
    fun listen(message: String) {
        // Assuming the message is in JSON format
        val event = parseTransactionEvent(message)
        if (event != null) {
            transactionMap.computeIfAbsent(event.atmId) { mutableListOf() }.add(event)
        }
        // Clean up old transactions
        cleanupOldTransactions()
    }

    fun getCustomerCount(atmId: String): Int {
        val now = LocalDateTime.now()
        val transactions = transactionMap[atmId]
        return transactions?.map { it.customerId }?.distinct()?.count { customerId ->
            transactions.any { it.customerId == customerId && it.timestamp.isAfter(now.minusHours(24)) }
        } ?: 0
    }

    fun getTransactionBreakdown(atmId: String): Map<TransactionType, Int> {
        val now = LocalDateTime.now()
        val transactions = transactionMap[atmId] ?: emptyList()
        return transactions.groupingBy { it.transactionType }.eachCount()
    }

    private fun parseTransactionEvent(message: String): TransactionEvent? {
        // Implement JSON parsing logic or message parsing logic
        // For simplicity, let's assume the message is a comma-separated string:
        // "atmId,customerId,timestamp,transactionType"

        val parts = message.split(",")
        return if (parts.size == 4) {
            val transactionType = when (parts[3].trim().uppercase()) {
                "DEPOSIT" -> TransactionType.DEPOSIT
                "CASH_WITHDRAWAL" -> TransactionType.CASH_WITHDRAWAL
                "BALANCE_INFORMATION_REQUEST" -> TransactionType.BALANCE_INFORMATION_REQUEST
                else -> null
            }
            if (transactionType != null) {
                TransactionEvent(
                    atmId = parts[0].trim(),
                    customerId = parts[1].trim(),
                    timestamp = LocalDateTime.parse(parts[2].trim()),
                    transactionType = transactionType
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun cleanupOldTransactions() {
        val cutoffTime = LocalDateTime.now().minusHours(24)
        transactionMap.values.forEach { transactions ->
            transactions.removeIf { it.timestamp.isBefore(cutoffTime) }
        }
    }

    // TODO: Maybe move to test class
    fun addMockTransaction(atmId: String, customerId: String, timestamp: LocalDateTime, transactionType: TransactionType) {
        val event = TransactionEvent(atmId, customerId, timestamp, transactionType)
        transactionMap.computeIfAbsent(atmId) { mutableListOf() }.add(event)
    }

    fun clearData() {
        transactionMap.clear()
    }
}
