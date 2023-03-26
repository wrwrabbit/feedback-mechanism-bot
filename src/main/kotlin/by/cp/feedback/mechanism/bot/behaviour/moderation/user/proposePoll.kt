package by.cp.feedback.mechanism.bot.behaviour.moderation.user

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessSevenDaysFromLastPollException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

fun proposePoll(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val langCode = "ru"
    val question = waitTextMessage(
        SendTextMessage(userId.toChatId(), "Отправьте вопрос")
    ).first().content.text
    val options = mutableListOf<String>()
    var option = waitTextMessage(
        SendTextMessage(userId.toChatId(), "Отправьте варианты ответа", replyMarkup = endMarkup())
    ).first().content.text
    while (option != "Завершить") {
        options.add(option)
        option = waitTextMessage(
            SendTextMessage(userId.toChatId(), "Отправьте варианты ответа", replyMarkup = endMarkup())
        ).first().content.text
    }
    val allowMultipleAnswers = waitTextMessage(
        SendTextMessage(userId.toChatId(), "Можно ли голоосовать за много вариантов?", replyMarkup = yesNoMarkup())
    ).first().content.text.lowercase().fromAllowMultipleAnswers(langCode)
    val lastUserPollTime = PollRepository.lastUserPoll(userId)?.createdAt
    if (lastUserPollTime != null) {
        val currentTime = LocalDateTime.now(ZoneOffset.UTC)
        val duration = Duration.between(currentTime, lastUserPollTime.plusSeconds(secondsBetweenPolls))
        if (duration.toSeconds() > 0 && duration.toSeconds() < secondsBetweenPolls) {
            throw LessSevenDaysFromLastPollException(
                timeTillNexPollText(duration.toDays(), duration.toHours(), duration.toMinutes(), langCode)
            )
        }
    }
    val savedPoll = PollRepository.save(userId, question, options.toTypedArray(), allowMultipleAnswers)
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            savedPoll.toMessage("be"),
            replyMarkup = moderatorsReviewMarkup(savedPoll.id)
        )
    )
    reply(message, sentToModeratorsText(langCode), replyMarkup = menuMarkup())
}

fun timeTillNexPollText(days: Long, hours: Long, minutes: Long, langCode: String) = when (langCode) {
    "be" -> "Час да наступнай магчымасці прапанаваць апытанне: дні=$days " +
        "гадзіны=$hours хвіліны=$minutes"

    else -> "Время до следующей возможности предложить опрос: дни=$days " +
        "часы=$hours минуты=$minutes"
}