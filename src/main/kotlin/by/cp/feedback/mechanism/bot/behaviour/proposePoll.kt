package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.exception.*
import by.cp.feedback.mechanism.bot.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollModerationRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.model.toMessage
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import io.ktor.util.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

fun proposePoll(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        try {
            val text = message.replyTo?.text ?: throw TextNotFoundInReplyException()
            val lines = text.split("\n")
            val question = lines.find { line -> line.startsWith("Вопрос: ") }?.substring(8)
                ?: throw QuestionNotFoundException()
            val options = lines.filter { line -> line.startsWith("Ответ: ") }
                .map { line -> line.substring(7) }
                .toTypedArray()
            if (options.size < 2) {
                throw LessThanTwoAnswersException()
            }
            val allowMultipleAnswers = lines.find { line -> line.startsWith("Больше одного ответа: ") }?.substring(22)
                ?.toLowerCasePreservingASCIIRules().let { string ->
                    when (string) {
                        "да" -> true
                        "нет" -> false
                        else -> null
                    }
                } ?: throw AllowMultipleAnswersNotFoundException()
            val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
            val lastUserPollTime = PollRepository.lastUserPoll(userId)?.createdAt
            if (lastUserPollTime != null) {
                val currentTime = LocalDateTime.now(ZoneOffset.UTC)
                val duration = Duration.between(currentTime, lastUserPollTime.plusDays(7))
                if (duration.toDays() < 7) {
                    throw LessSevenDaysFromLastPollException(
                        "Time till next available poll suggestion: days=${duration.toDays()} " +
                            "hours=${duration.toHours()} minutes=${duration.toMinutes()}"
                    )
                }
            }
            val savedPoll = PollRepository.save(userId, question, options, allowMultipleAnswers)
            PollModerationRepository.save(savedPoll.id)
            execute(SendTextMessage(moderatorsChatId.toChatId(), savedPoll.toMessage()))
            reply(message, "Your poll sent to moderators")
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }