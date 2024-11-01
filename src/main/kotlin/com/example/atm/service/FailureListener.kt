package com.example.atm.service

import com.example.atm.model.FailureEvent
import com.example.atm.model.FailureType
import com.example.atm.model.TransactionType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Service
class FailureListener {

    // In-memory data structure to store failure events
    private val failureMap = ConcurrentHashMap<String, MutableList<FailureEvent>>()

    @KafkaListener(topics = ["failure_logs"], groupId = "atm-monitoring-group")
    fun listen(message: String) {
        val event = parseFailureEvent(message)
        if (event != null) {
            failureMap.computeIfAbsent(event.atmId) { mutableListOf() }.add(event)
        }
    }

    fun getFailures(atmId: String): List<FailureEvent> {
        return failureMap[atmId]?.toList() ?: emptyList()
    }

    private fun parseFailureEvent(message: String): FailureEvent? {
        // Assuming the message is a comma-separated string:
        // "atmId,timestamp,failureType,transactionType,incidentContext,downtime"

        val parts = message.split(",", limit = 6)
        return if (parts.size == 6) {
            val failureType = when (parts[2].trim().uppercase()) {
                "HARDWARE_FAILURE" -> FailureType.HARDWARE_FAILURE
                "NETWORK_FAILURE" -> FailureType.NETWORK_FAILURE
                "SOFTWARE_FAILURE" -> FailureType.SOFTWARE_FAILURE
                else -> FailureType.UNKNOWN
            }
            val transactionType = when (parts[3].trim().uppercase()) {
                "DEPOSIT" -> TransactionType.DEPOSIT
                "CASH_WITHDRAWAL" -> TransactionType.CASH_WITHDRAWAL
                "BALANCE_INFORMATION_REQUEST" -> TransactionType.BALANCE_INFORMATION_REQUEST
                else -> null
            }
            val downtime = Duration.parse(parts[5].trim())

            FailureEvent(
                atmId = parts[0].trim(),
                timestamp = LocalDateTime.parse(parts[1].trim()),
                failureType = failureType,
                transactionType = transactionType,
                incidentContext = parts[4].trim(),
                downtime = downtime
            )
        } else {
            null
        }
    }

    // For testing purposes
    fun addMockFailure(event: FailureEvent) {
        failureMap.computeIfAbsent(event.atmId) { mutableListOf() }.add(event)
    }

    fun clearData() {
        failureMap.clear()
    }
}
