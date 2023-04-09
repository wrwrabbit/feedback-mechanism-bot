package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessSevenDaysFromLastPollException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

fun userProposePoll(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val lastUserPollTime = PollRepository.lastUserPoll(userId)?.createdAt
    if (lastUserPollTime != null) {
        val duration = Duration.between(lastUserPollTime, LocalDateTime.now(ZoneOffset.UTC))
        if (duration.toSeconds() < secondsBetweenPolls) {
            throw LessSevenDaysFromLastPollException(
                timeTillNexPollText(duration)
            )
        }
    }
    val question = waitTextMessage(
        SendTextMessage(userId.toChatId(), "Отправьте вопрос")
    ).filter { msg -> msg.sameThread(message) }.first().content.text
    val options = mutableListOf<String>()
    var option = waitTextMessage(
        SendTextMessage(userId.toChatId(), "Отправьте вариант ответа №${options.size + 1}", replyMarkup = endMarkup())
    ).filter { msg -> msg.sameThread(message) }.first().content.text
    while (option != "Завершить") {
        options.add(option)
        option = waitTextMessage(
            SendTextMessage(
                userId.toChatId(),
                "Отправьте вариант ответа №${options.size + 1}",
                replyMarkup = endMarkup()
            )
        ).filter { msg -> msg.sameThread(message) }.first().content.text
    }
    val allowMultipleAnswers = waitTextMessage(
        SendTextMessage(
            userId.toChatId(),
            "Можно ли голосовать за больше чем один вариант?",
            replyMarkup = yesNoMarkup()
        )
    ).filter { msg -> msg.sameThread(message) }.first().content.text.lowercase().fromAllowMultipleAnswers()
    val savedPoll = PollRepository.save(userId, question, options.toTypedArray(), allowMultipleAnswers)
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            savedPoll.toMessage(),
            replyMarkup = moderatorsReviewMarkup(savedPoll.id, 0)
        )
    )
    reply(message, sentToModeratorsText(), replyMarkup = menuMarkup())
}

fun timeTillNexPollText(duration: Duration): String {
    val stringBuilder = StringBuilder("Время до следующей возможности предложить опрос: ")
    duration.toDays().takeIf { it != 0L }?.let { stringBuilder.append(" дни=$it") }
    duration.toHours().takeIf { it != 0L }?.let { stringBuilder.append(" часы=${it - duration.toDays() * 24}") }
    duration.toMinutes().takeIf { it != 0L }?.let { stringBuilder.append(" минуты=${it - duration.toHours() * 60}") }
    duration.toSeconds().takeIf { it != 0L }?.let { stringBuilder.append(" секунды=${it - duration.toMinutes() * 60}") }
    return stringBuilder.toString().trim()
}
