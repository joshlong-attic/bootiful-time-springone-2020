package com.example.co_time

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@SpringBootApplication
class CoTimeApplication

fun main(args: Array<String>) = runBlocking<Unit> {
  runApplication<CoTimeApplication>(*args)
}

@Configuration
class CoRouterConfiguration {
  @Bean
  fun dslRoute(userRepo: UserRepo) = router {
    GET("/byId/{id}") {
      val body = runBlocking {userRepo.byId(it.pathVariable("id")).toMono() }
      ServerResponse
              .ok()
              .body(body, User::class.java)
    }
  }

  @Bean
  fun coRoute(userRepo: UserRepo) = coRouter {
    GET("/users/{id}") {
      val body: User = userRepo.byId(it.pathVariable("id"))
      ServerResponse.ok().bodyValueAndAwait(body)
    }

  }
}

data class User(val id: String, val name: String);

@Component
class UserRepo {
  suspend fun byId(id: String): User = User("1234", "SpringOne")
}