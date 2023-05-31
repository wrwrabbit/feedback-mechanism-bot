package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.captcha.CaptchaService
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.changeCaptcha
import by.cp.feedback.mechanism.bot.model.changeCaptchaMarkup
import by.cp.feedback.mechanism.bot.model.menuMarkup
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
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
    if (!UserRepository.exists(userId)) {
        var imageText = CaptchaService.getCaptcha()
        var userCaptchaMessage = waitCaptcha(userId, imageText, message)
        while (userCaptchaMessage.content.text != imageText.second) {
            if (userCaptchaMessage.content.text == changeCaptcha) {
                imageText = CaptchaService.getCaptcha()
                userCaptchaMessage = waitCaptcha(userId, imageText, message)
            } else {
                reply(userCaptchaMessage, wrongCaptchaText())
                userCaptchaMessage = waitCaptcha(userId, imageText, message)
            }
        }
        UserRepository.save(userId, "ru")
        PollUserReviewRepository.saveByUserId(userId)
        PollUserVoteRepository.saveByUserId(userId)
    }
    reply(message, helloText(), replyMarkup = menuMarkup())
}

private suspend fun BehaviourContext.waitCaptcha(
    userId: Long,
    imageText: Pair<BufferedImage, String>,
    message: CommonMessage<TextContent>
) = waitTextMessage(
    SendPhoto(
        userId.toChatId(),
        imageText.first.toPhoto(),
        sendMeCaptchaText(),
        replyMarkup = changeCaptchaMarkup()
    )
).filter { msg -> msg.sameThread(message) }.first()

fun BufferedImage.toPhoto() = ByteArrayOutputStream().let {
    ImageIO.write(this, "jpg", it)
    it.toByteArray()
}.asMultipartFile("captcha")

fun sendMeCaptchaText() = "Введите код с картинки"

fun wrongCaptchaText() = "Неправильная капча"

fun helloText() = "Создайте опрос, нажав на значёк \"Скрепка\" - иконка Опрос внизу; или через меню три точки в правом верхнем углу чата. Опрос будет анонимным."
