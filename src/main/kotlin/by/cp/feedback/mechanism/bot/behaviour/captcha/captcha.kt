package by.cp.feedback.mechanism.bot.behaviour.captcha

import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.model.changeCaptcha
import by.cp.feedback.mechanism.bot.model.changeCaptchaMarkup
import by.cp.feedback.mechanism.bot.model.sendMeCaptchaText
import by.cp.feedback.mechanism.bot.model.wrongCaptchaText
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.requests.send.media.SendPhoto
import dev.inmo.tgbotapi.types.ChatIdentifier
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

suspend fun BehaviourContext.captchaRequest(
    userId: Long,
    chatId: ChatIdentifier
) {
    var imageText = CaptchaService.getCaptcha()
    var userCaptchaMessage = waitCaptcha(userId, imageText, chatId)
    while (userCaptchaMessage.content.text.lowercase() != imageText.second.lowercase()) {
        if (userCaptchaMessage.content.text == changeCaptcha) {
            imageText = CaptchaService.getCaptcha()
            userCaptchaMessage = waitCaptcha(userId, imageText, chatId)
        } else {
            reply(userCaptchaMessage, wrongCaptchaText())
            userCaptchaMessage = waitCaptcha(userId, imageText, chatId)
        }
    }
}

private suspend fun BehaviourContext.waitCaptcha(
    userId: Long,
    imageText: Pair<BufferedImage, String>,
    chatId: ChatIdentifier
) = waitTextMessage(
    SendPhoto(
        userId.toChatId(),
        imageText.first.toPhoto(),
        sendMeCaptchaText(),
        replyMarkup = changeCaptchaMarkup()
    )
).filter { msg -> msg.sameThread(chatId) }.first()

fun BufferedImage.toPhoto() = ByteArrayOutputStream().let {
    ImageIO.write(this, "jpg", it)
    it.toByteArray()
}.asMultipartFile("captcha")
