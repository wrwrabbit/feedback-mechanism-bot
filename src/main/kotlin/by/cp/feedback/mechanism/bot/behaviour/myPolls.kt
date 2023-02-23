package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.langCode
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun myPolls(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val polls = PollRepository.getByUserId(userId)
    val response = polls.joinToString("\n") { it.toStatusMessage(message.langCode()) + "\n" }
    reply(message, response)
}

fun PollDto.toStatusMessage(langCode: String): String = when (langCode) {
    "be" -> "Апытанне #$id," +
        "Статус #$status" +
        if (status == PollStatus.REJECTED) ",Прычына адмовы: $rejectionReason" else ""

    else -> "Опрос #$id," +
        "Статус #$status" +
        if (status == PollStatus.REJECTED) ",Причина отказа: $rejectionReason" else ""
}
