package by.cp.feedback.mechanism.bot

import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitText
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
import org.jetbrains.exposed.sql.Database
import by.cp.feedback.mechanism.bot.repository.UserRepository

private val userRepository = UserRepository()

fun randomStringByKotlinRandom(length: Int) =
    List(length) { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")

suspend fun main() {
    Database.connect(
        System.getenv("DB_HOST") + System.getenv("DB_DATABASE"), driver = "com.impossibl.postgres.jdbc.PGDriver",
        user = System.getenv("DB_USER"), password = System.getenv("DB_PASSWORD")
    )
    val bot = telegramBot(System.getenv("TOKEN"))
    val behaviour = bot.buildBehaviour {
        onCommand("start") { message: CommonMessage<TextContent> ->
            message.from?.id?.let { userId ->
                if (userRepository.exists(userId.chatId)) {
                    reply(message, "Hello")
                } else {
                    val captcha = randomStringByKotlinRandom(4)
                    val userResponse = waitText(
                        SendTextMessage(userId.toChatId(), "Register, send me captcha: {$captcha}")
                    ).first().text
                    if (userResponse == captcha) {
                        userRepository.save(userId.chatId)
                    } else {
                        reply(message, "Wrong captcha")
                    }
                }
            }
        }
        onCommand("register") {

        }
    }

    bot.setWebhookInfoAndStartListenWebhooks(
        listenPort = 3000,
        engineFactory = Netty,
        setWebhookRequest = SetWebhook(url = System.getenv("WEBHOOK_URL")),
        exceptionsHandler = {
            it.printStackTrace()
        },
        block = behaviour.asUpdateReceiver
    )

}