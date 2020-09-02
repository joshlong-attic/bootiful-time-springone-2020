package com.example.co_time

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter

@WebFluxTest
@Import(WebTestConfiguration::class)
class CoTimeApplicationTests {

	@Autowired
	private lateinit var client: WebTestClient

	@MockBean
	private lateinit var userRepo: UserRepo

	fun <T> givenSuspended(block: suspend () -> T) = BDDMockito.given(runBlocking { block() })
	infix fun <T> BDDMockito.BDDMyOngoingStubbing<T>.willReturn(block: () -> T) = willReturn(block())

	@BeforeEach
	fun setUp() {
		val users = listOf(User("1234","SpringOne"))
		givenSuspended { userRepo.all() } willReturn { users.asFlow() }
	}

	@Test
	fun contextLoads() {
		client
				.get()
				.uri("/users/")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk
				.expectBody()
				.jsonPath("$.[0].name").isEqualTo("SpringOne")
	}
}

@Configuration
class WebTestConfiguration {

	@Bean
	fun mainRouter(userRepo: UserRepo) = coRouter {
		(GET("/user/") or GET("/users/")) {
			ServerResponse.ok().bodyAndAwait(userRepo.all())
		}
	}
}