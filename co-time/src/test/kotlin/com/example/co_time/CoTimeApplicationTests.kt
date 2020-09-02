package com.example.co_time

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@Import(CoRouterConfiguration::class, UserRepo::class)
class CoTimeApplicationTests (@Autowired val client:WebTestClient) {

  @Test
  fun contextLoads() {
    client
        .get()
        .uri("/users/23232")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.name")
        .isEqualTo("SpringOne")
  }
}
