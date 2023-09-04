package by.cp.feedback.mechanism.bot.table

import by.cp.feedback.mechanism.bot.model.MessageQueueType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object MessageQueue : Table(name = "message_queue") {
    val userId: Column<Long> = long("user_id")
    val pollId: Column<Long> = long("poll_id")
    val type: Column<MessageQueueType> = enumerationByName("type", 255, MessageQueueType::class)
}
