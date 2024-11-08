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
class TransactionControllerTest(
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

        // ATM with only old transactions (>24 hours)
        transactionListener.addMockTransaction("ATM_OLD_TRANSACTIONS", "CustomerOld", now.minusHours(26), TransactionType.DEPOSIT)

        // ATM with multiple transactions from the same customer
        transactionListener.addMockTransaction("ATM_DUPLICATE_CUSTOMER", "Customer001", now.minusHours(1), TransactionType.DEPOSIT)
        transactionListener.addMockTransaction("ATM_DUPLICATE_CUSTOMER", "Customer001", now.minusHours(2), TransactionType.CASH_WITHDRAWAL)
        transactionListener.addMockTransaction("ATM_DUPLICATE_CUSTOMER", "Customer001", now.minusHours(3), TransactionType.BALANCE_INFORMATION_REQUEST)

        // ATM with transaction exactly at 24-hour boundary
        transactionListener.addMockTransaction("ATM_TIME_BOUNDARY", "CustomerBoundary", now.minusHours(24), TransactionType.DEPOSIT)

        // ATM with multiple customers including duplicates
        transactionListener.addMockTransaction("ATM_MULTIPLE_CUSTOMERS", "CustomerA", now.minusHours(1), TransactionType.DEPOSIT)
        transactionListener.addMockTransaction("ATM_MULTIPLE_CUSTOMERS", "CustomerB", now.minusHours(2), TransactionType.CASH_WITHDRAWAL)
        transactionListener.addMockTransaction("ATM_MULTIPLE_CUSTOMERS", "CustomerA", now.minusHours(3), TransactionType.BALANCE_INFORMATION_REQUEST)
        transactionListener.addMockTransaction("ATM_MULTIPLE_CUSTOMERS", "CustomerC", now.minusHours(4), TransactionType.DEPOSIT)
        transactionListener.addMockTransaction("ATM_MULTIPLE_CUSTOMERS", "CustomerD", now.minusHours(25), TransactionType.CASH_WITHDRAWAL) // Should be ignored
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM001 as ADMIN`() {
        mockMvc.get("/atm/ATM001/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM001") }
                jsonPath("$.customerCountLast24Hours") { value(3) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM with no transactions`() {
        mockMvc.get("/atm/ATM_NO_TRANSACTIONS/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM_NO_TRANSACTIONS") }
                jsonPath("$.customerCountLast24Hours") { value(0) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM with only old transactions`() {
        mockMvc.get("/atm/ATM_OLD_TRANSACTIONS/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM_OLD_TRANSACTIONS") }
                jsonPath("$.customerCountLast24Hours") { value(0) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM with multiple transactions from same customer`() {
        mockMvc.get("/atm/ATM_DUPLICATE_CUSTOMER/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM_DUPLICATE_CUSTOMER") }
                jsonPath("$.customerCountLast24Hours") { value(1) }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM with transaction exactly 24 hours ago`() {
        mockMvc.get("/atm/ATM_TIME_BOUNDARY/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM_TIME_BOUNDARY") }
                jsonPath("$.customerCountLast24Hours") { value(0) } // Since it's not after 24 hours ago
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM with multiple customers and duplicates`() {
        mockMvc.get("/atm/ATM_MULTIPLE_CUSTOMERS/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM_MULTIPLE_CUSTOMERS") }
                jsonPath("$.customerCountLast24Hours") { value(3) } // CustomerA, CustomerB, CustomerC
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count with special characters in ATM ID`() {
        val atmId = "ATM@#$"
        mockMvc.get("/atm/{atmId}/customer-count", atmId)
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value(atmId) }
                jsonPath("$.customerCountLast24Hours") { value(0) }
            }
    }

    // TODO: More TCs?
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get transaction breakdown for ATM001 as ADMIN`() {
        mockMvc.get("/atm/ATM001/transaction-breakdown")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM001") }
                jsonPath("$.transactionBreakdownLast24Hours.DEPOSIT") { value(2) }
                jsonPath("$.transactionBreakdownLast24Hours.CASH_WITHDRAWAL") { value(1) }
                jsonPath("$.transactionBreakdownLast24Hours.BALANCE_INFORMATION_REQUEST") { 2 }
            }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Get customer count for ATM001 as USER should be forbidden`() {
        mockMvc.get("/atm/ATM001/customer-count")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `Get customer count without authentication should be unauthorized`() {
        mockMvc.get("/atm/ATM001/customer-count")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
