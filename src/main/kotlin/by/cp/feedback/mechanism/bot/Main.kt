package by.cp.feedback.mechanism.bot

import by.cp.feedback.mechanism.bot.exception.*
import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.repository.PollModerationRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.poll
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.requests.send.polls.SendRegularPoll
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

private val userRepository = UserRepository
private val pollRepository = PollRepository
private val pollModerationRepository = PollModerationRepository
private val moderatorsChatId = System.getenv("MODERATORS_CHAT_ID").toLong()
private const val approvalsRequired = 1

fun randomStringByKotlinRandom(length: Int) =
    List(length) { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")

suspend fun main() {
    val bot = telegramBot(System.getenv("TOKEN"))
    val behaviour = bot.buildBehaviour(
        defaultExceptionsHandler = {
            it.printStackTrace()
        }
    ) {
        onCommand("start", scenarioReceiver = start())
        onCommand("proposePoll", scenarioReceiver = proposePoll())
        onCommand("getChatId", scenarioReceiver = getChatId())
        onCommand("approve", scenarioReceiver = approve())
        onCommand("reject", scenarioReceiver = reject())
    }
    bot.setWebhookInfoAndStartListenWebhooks(
        listenPort = System.getenv("WEBHOOK_PORT").toInt(),
        listenRoute = System.getenv("WEBHOOK_ROUTE"),
        engineFactory = Netty,
        setWebhookRequest = SetWebhook(url = System.getenv("WEBHOOK_URL")),
        exceptionsHandler = {
            it.printStackTrace()
        },
        block = behaviour.asUpdateReceiver
    )
}

private fun Boolean.toAllowMultipleAnswers(): String =
    if (this) {
        "Да"
    } else {
        "Нет"
    }

private fun PollDto.toMessage(): String =
    """
        Вопрос: $question
        ${options.map { option -> "Ответ: $option\n" }}
        Больше одного ответа: ${allowMultipleAnswers.toAllowMultipleAnswers()}
    """.trimIndent()

private fun reject(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        reply(message, "ChatId={${message.chat.id.chatId}}")
    }

private fun approve(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        if (message.chat.id.chatId == moderatorsChatId) {
            reply(message, "You are not moderator")
            throw NotModeratorsChatException("You are not moderator")
        }
        val telegramPollId = message.replyTo?.poll?.id?.toLong()
        if (telegramPollId == null) {
            reply(message, "Poll not found in reply")
            throw PollNotFoundInReplyException("Poll not found in reply")
        }
        val pollDb = pollModerationRepository.getByTelegramId(telegramPollId)
        if (pollDb == null) {
            reply(message, "Poll not found in db")
            throw PollNotFoundInDbException("Poll not found in db")
        }
        if (pollDb.rejectionReason != null) {
            reply(message, "Can't approve rejected poll")
            throw CantApproveRejectedException("Can't approve rejected poll")
        }
        val userId: Long = if (message.from?.id?.chatId != null) {
            message.from?.id?.chatId!!
        } else {
            reply(message, "Something went wrong")
            throw FromNullException("User not found")
        }
        if (userId in pollDb.approves) {
            reply(message, "You already approved this poll")
            throw AlreadyApprovedException("You already approved this poll")
        }
        val resultArray = pollDb.approves.plus(userId)
        pollModerationRepository.updateApprovesByTelegramId(telegramPollId, resultArray)
        reply(message, "You approved this poll, current approves ${resultArray.size}/${approvalsRequired}")
        if (resultArray.size == approvalsRequired) {

        }
    }

private fun getChatId(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        reply(message, "ChatId={${message.chat.id.chatId}}")
    }

private fun proposePoll(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        val text = message.replyTo?.text
        if (text == null) {
            reply(message, "Text not found in reply")
            throw TextNotFoundInReplyException("Text not found in reply")
        }
        val lines = text.split("\n")
        val question = lines.find { line -> line.startsWith("Вопрос: ") }?.substring(8)
        if (question == null) {
            reply(message, "Question not found in reply")
            throw QuestionNotFoundException("Question not found in reply")
        }
        val options = lines.filter { line -> line.startsWith("Ответ: ") }
            .map { line -> line.substring(7) }
            .toTypedArray()
        if (options.size < 2) {
            reply(message, "Less than two answers")
            throw LessThanTwoAnswersException("Less than two answers")
        }
        val allowMultipleAnswers = lines.find { line -> line.startsWith("Больше одного ответа: ") }?.substring(22)
            ?.toLowerCasePreservingASCIIRules().let { string ->
                when (string) {
                    "да" -> true
                    "нет" -> false
                    else -> null
                }
            }
        if (allowMultipleAnswers == null) {
            reply(message, "AllowMultipleAnswers not found in reply")
            throw AllowMultipleAnswersNotFoundException("AllowMultipleAnswers not found in reply")
        }
        val userId: Long = if (message.from?.id?.chatId != null) {
            message.from?.id?.chatId!!
        } else {
            reply(message, "Something went wrong")
            throw FromNullException("User not found")
        }
        val lastUserPollTime = pollRepository.lastUserPoll(userId)?.createdAt
        if (lastUserPollTime != null) {
            val currentTime = LocalDateTime.now(ZoneOffset.UTC)
            val duration = Duration.between(lastUserPollTime, currentTime)
            if (duration.toDays() < 7) {
                reply(
                    message,
                    "Time till next available poll suggestion: days=${duration.toDays()} " +
                        "hours=${duration.toHours()} minutes=${duration.toMinutes()}"
                )
            } else {
                val savedPoll = pollRepository.save(userId, question, options, allowMultipleAnswers)
                reply(message, "Your poll sent to moderators")
                val request = SendTextMessage(moderatorsChatId.toChatId(), savedPoll.toMessage())
                val telegramId = execute(request).messageId
                pollModerationRepository.save(savedPoll.id, telegramId)
            }
        } else {
            val savedPoll = pollRepository.save(userId, question, options, allowMultipleAnswers)
            reply(message, "Your poll sent to moderators")
            val request = SendTextMessage(moderatorsChatId.toChatId(), savedPoll.toMessage())
            val telegramId = execute(request).messageId
            pollModerationRepository.save(savedPoll.id, telegramId)
        }
    }

private fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        val userId: Long = if (message.from?.id?.chatId != null) {
            message.from?.id?.chatId!!
        } else {
            reply(message, "Something went wrong")
            throw FromNullException("User not found")
        }
        if (userRepository.exists(userId)) {
            reply(message, "Hello")
        } else {
            val expectedCaptcha = randomStringByKotlinRandom(4)
            var userCaptchaMessage = waitTextMessage(
                SendTextMessage(userId.toChatId(), "Register, send me captcha: {$expectedCaptcha}")
            ).first()
            while (userCaptchaMessage.content.text != expectedCaptcha) {
                reply(userCaptchaMessage, "Wrong captcha")
                userCaptchaMessage = waitTextMessage(
                    SendTextMessage(userId.toChatId(), "Register, send me captcha: {$expectedCaptcha}")
                ).first()
            }
            userRepository.save(userId)
            reply(userCaptchaMessage, "Hello")
        }
    }