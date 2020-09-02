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
import reactor.core.publisher.Mono

@SpringBootApplication
class CoTimeApplication

fun main(args: Array<String>) = runBlocking<Unit> {
  runApplication<CoTimeApplication>(*args)
}

@Configuration
class CoRouterConfiguration {

  @Bean
  fun mainRouter(userRepo: UserRepo) = coRouter {
    GET("/users/{id}") {
      val body: User = userRepo.byId(it.pathVariable("id"))
      ServerResponse.ok().bodyValueAndAwait(body)
    }

  }
}

data class User(val id: String, val name: String);

@Component
class UserRepo {
  suspend fun byId(id: String): User = Mono.just(User("1234", "SpringOne")).awaitFirst()
}