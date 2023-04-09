package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun myPolls(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val polls = PollRepository.getByUserId(userId)
    val pollsResponse = polls.joinToString("\n") { it.toStatusMessage() + "\n" }
    val response = if (pollsResponse.isNotEmpty()) {
        pollsResponse
    } else {
        emptyPollsMessage()
    }
    reply(message, response)
}

fun emptyPollsMessage(): String = "У вас нет опросов"

fun PollDto.toStatusMessage(): String = "Опрос #$id," + "\n" +
        "Вопрос $question," + "\n" +
        "Статус #$status" + "\n" +
        if (status == PollStatus.REJECTED) ",Причина отказа: $rejectionReason" else ""
