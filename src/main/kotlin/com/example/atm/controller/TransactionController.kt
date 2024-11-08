package com.example.atm.controller

import com.example.atm.service.TransactionListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TransactionController(val transactionListener: TransactionListener) {

    @GetMapping("/atm/{atmId}/customer-count")
    @PreAuthorize("hasRole('ADMIN')")
    fun getCustomerCount(@PathVariable("atmId") atmId: String): Map<String, Any> {
        val count = transactionListener.getCustomerCount(atmId)
        return mapOf(
            "atmId" to atmId,
            "customerCountLast24Hours" to count
        )
    }

    @GetMapping("/atm/{atmId}/transaction-breakdown")
    @PreAuthorize("hasRole('ADMIN')")
    fun getTransactionBreakdown(@PathVariable("atmId") atmId: String): Map<String, Any> {
        val breakdown = transactionListener.getTransactionBreakdown(atmId)
        val formattedBreakdown = breakdown.mapKeys { it.key.name }
        return mapOf(
            "atmId" to atmId,
            "transactionBreakdown" to formattedBreakdown
        )
    }
}
