package com.example.atm

import com.example.atm.model.TransactionType
import com.example.atm.service.listener.TransactionListener
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTransactionBreakdownTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val transactionListener: TransactionListener
) {

    @BeforeEach
    fun setup() {
        transactionListener.clearData()

        val now = LocalDateTime.now()

        transactionListener.addMockTransaction("ATM001", "Customer001", now.minusHours(1), TransactionType.DEPOSIT)
        transactionListener.addMockTransaction("ATM001", "Customer002", now.minusHours(2), TransactionType.CASH_WITHDRAWAL)
        transactionListener.addMockTransaction("ATM001", "Customer003", now.minusHours(25), TransactionType.BALANCE_INFORMATION_REQUEST) // Should be ignored
        transactionListener.addMockTransaction("ATM001", "Customer005", now.minusHours(3), TransactionType.DEPOSIT)
        transactionListener.addMockTransaction("ATM002", "Customer004", now.minusHours(3), TransactionType.CASH_WITHDRAWAL)
        transactionListener.addMockTransaction("ATM001", "Customer002", now.minusHours(4), TransactionType.BALANCE_INFORMATION_REQUEST)
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get transaction breakdown for ATM001 as ADMIN`() {
        mockMvc.get("/atm/ATM001/transaction-breakdown")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM001") }
                jsonPath("$.transactionBreakdown.DEPOSIT") { value(2) }
                jsonPath("$.transactionBreakdown.CASH_WITHDRAWAL") { value(1) }
                jsonPath("$.transactionBreakdown.BALANCE_INFORMATION_REQUEST") { value(2) }
            }
    }

    @Test
    fun `Get transaction breakdown without authentication`() {
        mockMvc.get("/atm/ATM001/transaction-breakdown")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Get transaction breakdown as non-admin user`() {
        mockMvc.get("/atm/ATM001/transaction-breakdown")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get transaction breakdown for non-existent ATM`() {
        mockMvc.get("/atm/ATM999/transaction-breakdown")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM999") }
                jsonPath("$.transactionBreakdown") { isEmpty() }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get transaction breakdown for ATM002`() {
        mockMvc.get("/atm/ATM002/transaction-breakdown")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM002") }
                jsonPath("$.transactionBreakdown.DEPOSIT") { doesNotExist() }
                jsonPath("$.transactionBreakdown.CASH_WITHDRAWAL") { value(1) }
                jsonPath("$.transactionBreakdown.BALANCE_INFORMATION_REQUEST") { doesNotExist() }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get transaction breakdown with invalid atmId`() {
        val atmId = "ATM@#$"
        mockMvc.get("/atm/{atmId}/transaction-breakdown", atmId)
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value(atmId) }
                jsonPath("$.transactionBreakdown") { isEmpty() }
            }
    }
}
