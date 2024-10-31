package com.example.atm

import com.example.atm.service.TransactionListener
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
        val now = LocalDateTime.now()
        transactionListener.addMockTransaction("ATM001", "Customer001", now.minusHours(1))
        transactionListener.addMockTransaction("ATM001", "Customer002", now.minusHours(2))
        transactionListener.addMockTransaction("ATM001", "Customer003", now.minusHours(25)) // Should be ignored
        transactionListener.addMockTransaction("ATM002", "Customer004", now.minusHours(3))
    }

    // TODO: More TCs?
    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Get customer count for ATM001 as ADMIN`() {
        mockMvc.get("/atm/ATM001/customer-count")
            .andExpect {
                status { isOk() }
                jsonPath("$.atmId") { value("ATM001") }
                jsonPath("$.customerCountLast24Hours") { value(2) }
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
