package com.example.atm

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GreetingController {

    @GetMapping("/public")
    fun publicEndpoint() = "This is a public endpoint."

    @GetMapping("/user")
    fun userEndpoint() = "This is a user endpoint."

    @GetMapping("/admin")
    fun adminEndpoint() = "This is an admin endpoint."
}
