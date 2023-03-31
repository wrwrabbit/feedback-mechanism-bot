package by.cp.feedback.mechanism.bot.behaviour.moderation

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

fun moderationPolls(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val langCode = "ru"
    val polls = PollRepository.getByStatus(PollStatus.ON_MODERATOR_REVIEW)
    val pollsResponse = polls.joinToString("\n") { it.toStatusMessage(langCode) + "\n" }
    val response = pollsResponse.ifEmpty {
        emptyPollsMessage(langCode)
    }
    reply(message, response)
}

fun emptyPollsMessage(langCode: String): String = when (langCode) {
    "be" -> "У вас няма апытанняў"
    else -> "Нет вопросов для модерации"
}

fun PollDto.toStatusMessage(langCode: String): String = when (langCode) {
    "be" -> "Апытанне #$id," +
        "Статус #$status" +
        if (status == PollStatus.REJECTED) ",Прычына адмовы: $rejectionReason" else ""

    else -> "Опрос #$id," + "\n" +
        "Вопрос $question," + "\n" +
        "Статус #$status" + "\n"
}
