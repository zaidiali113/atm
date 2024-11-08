package com.example.atm.controller

import com.example.atm.service.listener.FailureListener
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class FailureController(val failureListener: FailureListener) {

    @GetMapping("/atm/{atmId}/failures")
    @PreAuthorize("hasRole('ADMIN')")
    fun getFailures(@PathVariable("atmId") atmId: String): Map<String, Any> {
        val failures = failureListener.getFailures(atmId)
        return mapOf(
            "atmId" to atmId,
            "failures" to failures
        )
    }
}
