package com.example.atm

import com.example.atm.model.*
import com.example.atm.service.listener.FailureListener
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Duration
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class FailureControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val failureListener: FailureListener
) {

    @BeforeEach
    fun setup() {
        val now = LocalDateTime.now()

        // Clear any existing data
        failureListener.clearData()

        // Add mock failure events with provided downtime
        val failureEvent1 = FailureEvent(
            atmId = "ATM001",
            timestamp = now.minusHours(5),
            failureType = FailureType.HARDWARE_FAILURE,
            transactionType = null,
            incidentContext = "ATM screen not responding",
            downtime = Duration.ofHours(1) // 1 hour downtime
        )
        failureListener.addMockFailure(failureEvent1)

        val failureEvent3 = FailureEvent(
            atmId = "ATM001",
            timestamp = now.minusHours(5),
            failureType = FailureType.NETWORK_FAILURE,
            transactionType = TransactionType.CASH_WITHDRAWAL,
            incidentContext = "Network timeout",
            downtime = Duration.ofMinutes(30)
        )
        failureListener.addMockFailure(failureEvent3)

        val failureEvent2 = FailureEvent(
            atmId = "ATM002",
            timestamp = now.minusHours(2),
            failureType = FailureType.NETWORK_FAILURE,
            transactionType = TransactionType.CASH_WITHDRAWAL,
            incidentContext = "Network timeout",
            downtime = Duration.ofMinutes(30) // 30 minutes downtime
        )
        failureListener.addMockFailure(failureEvent2)
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get failures for ATM001 as ADMIN`() {
        mockMvc.get("/atm/ATM001/failures")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM001") }
                jsonPath("$.failures") { isArray() }
                jsonPath("$.failures[0].failureType") { value("HARDWARE_FAILURE") }
                jsonPath("$.failures[0].downtime") { value("PT1H") } // Downtime in ISO-8601 format

                jsonPath("$.failures[1].failureType") { value("NETWORK_FAILURE") }
                jsonPath("$.failures[1].downtime") { value("PT30M") } // Downtime in ISO-8601 format
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get failures for ATM002 as ADMIN`() {
        mockMvc.get("/atm/ATM002/failures")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM002") }
                jsonPath("$.failures") { isArray() }
                jsonPath("$.failures[0].failureType") { value("NETWORK_FAILURE") }
                jsonPath("$.failures[0].downtime") { value("PT30M") } // Downtime in ISO-8601 format
            }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Get failures for ATM001 as USER should be forbidden`() {
        mockMvc.get("/atm/ATM001/failures")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `Get failures without authentication should be unauthorized`() {
        mockMvc.get("/atm/ATM001/failures")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
