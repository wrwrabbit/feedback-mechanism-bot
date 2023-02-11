package by.cp.feedback.mechanism.bot

import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import io.ktor.server.netty.*
import kotlinx.coroutines.flow.first

private val userRepository = UserRepository()

fun randomStringByKotlinRandom(length: Int) =
    List(length) { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")

suspend fun main() {
    val bot = telegramBot(System.getenv("TOKEN"))
    val behaviour = bot.buildBehaviour {
        onCommand("start", scenarioReceiver = start())
        onCommand("register") {

        }
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

private fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        message.from?.id?.let { userId ->
            if (userRepository.exists(userId.chatId)) {
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
                userRepository.save(userId.chatId)
                SendTextMessage(userId.toChatId(), "Register, send me captcha: {$expectedCaptcha}")
                reply(userCaptchaMessage, "Hello")
            }
        }
    }