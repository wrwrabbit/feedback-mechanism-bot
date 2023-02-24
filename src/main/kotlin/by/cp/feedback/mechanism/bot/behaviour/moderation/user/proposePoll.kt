package by.cp.feedback.mechanism.bot.behaviour.moderation.user

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessSevenDaysFromLastPollException
import by.cp.feedback.mechanism.bot.exception.TextNotFoundInReplyException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

fun sendToModeratorsReview(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val text = message.replyTo?.text ?: throw TextNotFoundInReplyException()
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val langCode = UserRepository.langCodeById(userId)
    val (question, options, allowMultipleAnswers) = parsePoll(text, langCode)
    val lastUserPollTime = PollRepository.lastUserPoll(userId)?.createdAt
    if (lastUserPollTime != null) {
        val currentTime = LocalDateTime.now(ZoneOffset.UTC)
        val duration = Duration.between(currentTime, lastUserPollTime.plusDays(7))
        if (duration.toDays() < 7) {
            throw LessSevenDaysFromLastPollException(
                timeTillNexPollText(duration.toDays(), duration.toHours(), duration.toMinutes(), langCode)
            )
        }
    }
    val savedPoll = PollRepository.save(userId, question, options, allowMultipleAnswers)
    val markup = InlineKeyboardMarkup(
        matrix {
            row {
                +CallbackDataInlineKeyboardButton(
                    "✅ 0/$moderatorsApprovalsRequired",
                    callbackData = "$moderatorApproveDataCallback${savedPoll.id}"
                )
            }
        }
    )
    execute(SendTextMessage(moderatorsChatId.toChatId(), savedPoll.toMessage("be"), replyMarkup = markup))
    reply(message, sentToModeratorsText(langCode))
}

fun timeTillNexPollText(days: Long, hours: Long, minutes: Long, langCode: String) = when (langCode) {
    "be" -> "Час да наступнай магчымасці прапанаваць апытанне: дні=$days " +
        "гадзіны=$hours хвіліны=$minutes"

    else -> "Время до следующей возможности предложить опрос: дни=$days " +
        "часы=$hours минуты=$minutes"
}