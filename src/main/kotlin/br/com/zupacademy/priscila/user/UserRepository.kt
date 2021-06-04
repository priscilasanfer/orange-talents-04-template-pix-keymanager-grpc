package br.com.zupacademy.priscila.user

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface UserRepository : JpaRepository<UserDb, Long> {
}