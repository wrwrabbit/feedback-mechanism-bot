package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.changeCaptcha
import by.cp.feedback.mechanism.bot.model.changeCaptchaMarkup
import by.cp.feedback.mechanism.bot.model.menuMarkup
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.requests.send.media.SendPhoto
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val langCode = "ru"
    if (!UserRepository.exists(userId)) {
        var imageText = CaptchaService.getCaptcha()
        var userCaptchaMessage = waitCaptcha(userId, imageText, langCode, message)
        while (userCaptchaMessage.content.text != imageText.second) {
            if (userCaptchaMessage.content.text == changeCaptcha) {
                imageText = CaptchaService.getCaptcha()
                userCaptchaMessage = waitCaptcha(userId, imageText, langCode, message)
            } else {
                reply(userCaptchaMessage, wrongCaptchaText(langCode))
                userCaptchaMessage = waitCaptcha(userId, imageText, langCode, message)
            }
        }
        UserRepository.save(userId, langCode)
    }
    reply(message, helloText(langCode), replyMarkup = menuMarkup())
}

private suspend fun BehaviourContext.waitCaptcha(
    userId: Long,
    imageText: Pair<BufferedImage, String>,
    langCode: String,
    message: CommonMessage<TextContent>
) = waitTextMessage(
    SendPhoto(
        userId.toChatId(),
        imageText.first.toPhoto(),
        sendMeCaptchaText(langCode),
        replyMarkup = changeCaptchaMarkup()
    )
).filter { msg -> msg.sameThread(message) }.first()

fun BufferedImage.toPhoto() = ByteArrayOutputStream().let {
    ImageIO.write(this, "jpg", it)
    it.toByteArray()
}.asMultipartFile("captcha")

fun sendMeCaptchaText(langCode: String) = when (langCode) {
    "be" -> "Адышліце капчу"
    else -> "Отошлите капчу"
}

fun wrongCaptchaText(langCode: String) = when (langCode) {
    "be" -> "Неправільная капча"
    else -> "Неправильная капча"
}

fun helloLangText(langCode: String) = when (langCode) {
    "be" -> "Прывітанне\nАбярыце мову"
    else -> "Привет\nВыберете язык"
}

fun helloText(langCode: String) = when (langCode) {
    "be" -> "Прывітанне"
    else -> "Привет"
}
