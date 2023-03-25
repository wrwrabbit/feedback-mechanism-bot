package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun myPolls(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    // TODO return on behaviour finish
//    val langCode = UserRepository.langCodeById(userId)
    val langCode = "ru"
    val polls = PollRepository.getByUserId(userId)
    val pollsResponse = polls.joinToString("\n") { it.toStatusMessage(langCode) + "\n" }
    val response = if (pollsResponse.isNotEmpty()) {
        pollsResponse
    } else {
        emptyPollsMessage(langCode)
    }
    reply(message, response)
}

fun emptyPollsMessage(langCode: String): String = when (langCode) {
    "be" -> "У вас няма апытанняў"
    else -> "У вас нет опросов"
}

fun PollDto.toStatusMessage(langCode: String): String = when (langCode) {
    "be" -> "Апытанне #$id," +
        "Статус #$status" +
        if (status == PollStatus.REJECTED) ",Прычына адмовы: $rejectionReason" else ""

    else -> "Опрос #$id," +
        "Статус #$status" +
        if (status == PollStatus.REJECTED) ",Причина отказа: $rejectionReason" else ""
}
