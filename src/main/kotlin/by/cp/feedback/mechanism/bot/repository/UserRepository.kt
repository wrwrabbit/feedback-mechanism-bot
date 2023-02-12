package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import by.cp.feedback.mechanism.bot.table.Users

object UserRepository {

    private val db = DatabaseConfiguration.database

    fun save(userId: Long) = transaction {
        Users.insertAndGetId {
            it[Users.id] = userId
        }.value
    }

    fun exists(userId: Long): Boolean = transaction {
        !Users.select { Users.id eq userId }.empty()
    }

}