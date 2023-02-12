package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.requests.send.media.SendPhoto
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    if (UserRepository.exists(userId)) {
        reply(message, "Hello")
    }
    val (image, text) = CaptchaService.getCaptcha()
    val file = ByteArrayOutputStream().let {
        ImageIO.write(image, "jpg", it)
        it.toByteArray()
    }.asMultipartFile("captcha")
    var userCaptchaMessage = waitTextMessage(
        SendPhoto(userId.toChatId(), file, "Send me captcha")
    ).first()
    while (userCaptchaMessage.content.text != text) {
        reply(userCaptchaMessage, "Wrong captcha")
        userCaptchaMessage = waitTextMessage(
            SendPhoto(userId.toChatId(), file, "Send me captcha")
        ).first()
    }
    UserRepository.save(userId)
    reply(userCaptchaMessage, "Hello")
}
