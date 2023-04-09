package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    private val db = DatabaseConfiguration.database

    fun save(userId: Long, langCode: String) = transaction {
        Users.insertAndGetId {
            it[Users.id] = userId
            it[Users.langCode] = langCode
        }.value
    }

    fun exists(userId: Long): Boolean = transaction {
        !Users.select { Users.id eq userId }.empty()
    }

}