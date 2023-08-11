package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.captcha.captchaRequest
import by.cp.feedback.mechanism.bot.behaviour.utils.tryFUser
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessSevenDaysFromLastPollException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.poll
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.PollContent
import dev.inmo.tgbotapi.types.polls.MultipleAnswersPoll
import dev.inmo.tgbotapi.types.toChatId
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

fun userProposePoll(): suspend BehaviourContext.(CommonMessage<PollContent>) -> Unit = tryFUser { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    checkTime(userId)
    val userPoll = message.poll!!
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
    if (UserRepository.captchaRequired(userId)) {
        captchaRequest(userId, message.chat.id)
    }
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            savedPoll.toModeratorsMessage(),
            replyMarkup = moderatorsReviewMarkup(savedPoll.id, 0)
        )
    )
    reply(message, sentToModeratorsText(savedPoll), replyMarkup = menuMarkup())
    UserRepository.pollCountInc(userId)
    delete(message)
}

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
