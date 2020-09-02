package com.example.co_time

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitLast
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Flux

@SpringBootApplication
class CoTimeApplication

fun main(args: Array<String>) = runBlocking<Unit> {
    runApplication<CoTimeApplication>(*args)
}

@Configuration
class CoRouterConfiguration {

    @Bean
    fun mainRouter(userRepo: UserRepo) = coRouter {
        (GET("/user/") or GET("/users/")) {
            ServerResponse.ok().bodyAndAwait(userRepo.all())
        }
    }
}

data class User(val id: String, val name: String);
data class NewUser(val name: String)

interface UserRepo {
    suspend fun all(): Flow<User>
    suspend fun byId(id: String): User
    suspend fun create(user: NewUser): User
}

@Component
class DefaultUserRepo : UserRepo {
    override suspend fun all() = Flux.fromIterable(listOf(User("1234", "SpringOne"))).asFlow()
    override suspend fun byId(id: String) = User("1234", "SpringOne")
    override suspend fun create(user: NewUser) = User("2345", user.name)
}