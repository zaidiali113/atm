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
}
