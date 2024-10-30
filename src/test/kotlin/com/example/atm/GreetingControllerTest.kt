package com.example.atm

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class GreetingControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun `Access public endpoint without authentication`() {
        mockMvc.get("/public")
            .andExpect {
                status { isOk() }
                content { string("This is a public endpoint.") }
            }
    }

    @Test
    fun `Access user endpoint without authentication`() {
        mockMvc.get("/user")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Access user endpoint with USER role`() {
        mockMvc.get("/user")
            .andExpect {
                status { isOk() }
                content { string("This is a user endpoint.") }
            }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Access admin endpoint with USER role`() {
        mockMvc.get("/admin")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Access admin endpoint with ADMIN role`() {
        mockMvc.get("/admin")
            .andExpect {
                status { isOk() }
                content { string("This is an admin endpoint.") }
            }
    }

    @Test
    fun `Access admin endpoint without authentication`() {
        mockMvc.get("/admin")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}
