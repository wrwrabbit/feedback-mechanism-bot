package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.exception.FeedbackBotException
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.first

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        try {
            val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
            if (UserRepository.exists(userId)) {
                reply(message, "Hello")
            }
            val expectedCaptcha = CaptchaService.getCaptcha()
            var userCaptchaMessage = waitTextMessage(
                SendTextMessage(userId.toChatId(), "Register, send me captcha: {$expectedCaptcha}")
            ).first()
            while (userCaptchaMessage.content.text != expectedCaptcha) {
                reply(userCaptchaMessage, "Wrong captcha")
                userCaptchaMessage = waitTextMessage(
                    SendTextMessage(userId.toChatId(), "Register, send me captcha: {$expectedCaptcha}")
                ).first()
            }
            UserRepository.save(userId)
            reply(userCaptchaMessage, "Hello")
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }