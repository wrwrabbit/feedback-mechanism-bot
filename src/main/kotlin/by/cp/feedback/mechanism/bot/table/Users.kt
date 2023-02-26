package by.cp.feedback.mechanism.bot.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object Users : LongIdTable(name = "users") {
    val langCode: Column<String> = text("lang_code")
}
