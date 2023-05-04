package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.tryFUser
import by.cp.feedback.mechanism.bot.botCommands
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessSevenDaysFromLastPollException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitPollMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.polls.MultipleAnswersPoll
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

fun userProposePoll(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryFUser { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    checkTime(userId)
    val userPoll = waitPollMessage(SendTextMessage(userId.toChatId(), "Отправьте опрос"))
        .filter { msg -> msg.sameThread(message) }.first().content.poll
    val question = userPoll.question
    val options = userPoll.options.map { it.text }.toTypedArray()
    val allowMultipleAnswers = if (userPoll is MultipleAnswersPoll) {
        userPoll.allowMultipleAnswers
    } else {
        false
    }
    val savedPoll = PollRepository.save(
        userId,
        question,
        options,
        allowMultipleAnswers
    )
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            savedPoll.toModeratorsMessage(),
            replyMarkup = moderatorsReviewMarkup(savedPoll.id, 0)
        )
    )
    reply(message, sentToModeratorsText(), replyMarkup = menuMarkup())
}

private suspend fun BehaviourContext.getAllowMultipleAnswers(
    userId: Long,
    message: CommonMessage<TextContent>
) = waitTextMessage(
    SendTextMessage(
        userId.toChatId(),
        "Можно ли голосовать за больше чем один вариант? Нажмите на кнопку \"Да\" или \"Нет\"",
        replyMarkup = yesNoMarkup()
    )
).filter { msg -> msg.sameThread(message) }.filter { msg -> msg.content.text !in botCommands }
    .first().content.text

private suspend fun BehaviourContext.getOption(
    userId: Long,
    options: MutableList<String>,
    message: CommonMessage<TextContent>
) = waitTextMessage(
    SendTextMessage(
        userId.toChatId(),
        "Отправьте вариант ответа №${options.size + 1}(максимальное количество - 10)",
        replyMarkup = endMarkup().takeIf { options.size > 1 } ?: cancelPollCreationMarkup()
    )
).filter { msg -> msg.sameThread(message) }.filter { msg -> msg.content.text !in botCommands }.first().content.text

private suspend fun BehaviourContext.getQuestion(
    userId: Long,
    message: CommonMessage<TextContent>
) = waitTextMessage(
    SendTextMessage(userId.toChatId(), "Отправьте вопрос", replyMarkup = cancelPollCreationMarkup())
).filter { msg -> msg.sameThread(message) }.filter { msg -> msg.content.text !in botCommands }.first().content.text

private fun checkTime(userId: Long) {
    val lastUserPollTime = PollRepository.lastUserPoll(userId)?.createdAt
    if (lastUserPollTime != null) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        val duration = Duration.between(lastUserPollTime, now)
        if (duration.toSeconds() < secondsBetweenPolls) {
            throw LessSevenDaysFromLastPollException(
                timeTillNexPollText(Duration.between(now, lastUserPollTime.plusSeconds(secondsBetweenPolls)))
            )
        }
    }
}

fun timeTillNexPollText(duration: Duration): String {
    val stringBuilder = StringBuilder("Время до следующей возможности предложить опрос: ")
    duration.toDays().takeIf { it != 0L }?.let { stringBuilder.append(" дни=$it") }
    duration.toHours().takeIf { it != 0L }?.let { stringBuilder.append(" часы=${it - duration.toDays() * 24}") }
    duration.toMinutes().takeIf { it != 0L }?.let { stringBuilder.append(" минуты=${it - duration.toHours() * 60}") }
    duration.toSeconds().takeIf { it != 0L }?.let { stringBuilder.append(" секунды=${it - duration.toMinutes() * 60}") }
    return stringBuilder.toString().trim()
}
