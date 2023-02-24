package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.langCode
import by.cp.feedback.mechanism.bot.model.languageDataCallback
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.abstracts.asMultipartFile
import dev.inmo.tgbotapi.requests.send.media.SendPhoto
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val langCode = message.langCode()
    if (UserRepository.exists(userId)) {
        reply(message, helloText(langCode))
    } else {
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
        reply(userCaptchaMessage, helloText(langCode), replyMarkup = InlineKeyboardMarkup(
            matrix {
                row {
                    +CallbackDataInlineKeyboardButton(
                        "\uD83E\uDD0D❤️\uD83E\uDD0D",
                        callbackData = "${languageDataCallback}be"
                    )
                    +CallbackDataInlineKeyboardButton(
                        "\uD83E\uDD0D\uD83D\uDC99❤️",
                        callbackData = "${languageDataCallback}ru"
                    )
                }
            }
        ))
    }
}

fun sendMeCaptchaText(langCode: String) = when (langCode) {
    "be" -> "Адышліце капчу"
    else -> "Отошлите капчу"
}

fun wrongCaptchaText(langCode: String) = when (langCode) {
    "be" -> "Неправільная капча"
    else -> "Неправильная капча"
}

fun helloText(langCode: String) = when (langCode) {
    "be" -> "Прывітанне\nАбярыце мову"
    else -> "Привет\nВыберете язык"
}
