package com.example.atm.service

import com.example.atm.model.TransactionEvent
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

    private fun parseTransactionEvent(message: String): TransactionEvent? {
        val parts = message.split(",")
        return if (parts.size == 3) {
            TransactionEvent(
                atmId = parts[0],
                customerId = parts[1],
                timestamp = LocalDateTime.parse(parts[2])
            )
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
    fun addMockTransaction(atmId: String, customerId: String, timestamp: LocalDateTime) {
        val event = TransactionEvent(atmId, customerId, timestamp)
        transactionMap.computeIfAbsent(atmId) { mutableListOf() }.add(event)
    }
}
