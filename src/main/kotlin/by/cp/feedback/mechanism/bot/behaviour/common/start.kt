package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.menuMarkup
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.requests.send.media.SendPhoto
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    // TODO return on behaviour finish
//    val langCode = UserRepository.langCodeById(userId)
    val langCode = "ru"
    if (!UserRepository.exists(userId)) {
        val (image, text) = CaptchaService.getCaptcha()
        val file = ByteArrayOutputStream().let {
            ImageIO.write(image, "jpg", it)
            it.toByteArray()
        }.asMultipartFile("captcha")
        var userCaptchaMessage = waitTextMessage(
            SendPhoto(userId.toChatId(), file, sendMeCaptchaText(langCode))
        ).first()
        while (userCaptchaMessage.content.text != text) {
            reply(userCaptchaMessage, wrongCaptchaText(langCode))
            userCaptchaMessage = waitTextMessage(
                SendPhoto(userId.toChatId(), file, sendMeCaptchaText(langCode))
            ).first()
        }
        UserRepository.save(userId, langCode)
    }
    reply(message, helloText(langCode), replyMarkup = menuMarkup())
}

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
