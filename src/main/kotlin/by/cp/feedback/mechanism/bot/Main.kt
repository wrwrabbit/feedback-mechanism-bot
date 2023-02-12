package by.cp.feedback.mechanism.bot

import by.cp.feedback.mechanism.bot.behaviour.*
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import dev.inmo.tgbotapi.types.BotCommand
import io.ktor.server.netty.*

val moderatorsChatId = System.getenv("MODERATORS_CHAT_ID").toLong()
const val approvalsRequired = 1
private const val start = "start"
private const val proposePoll = "propose_poll"
private const val getChatId = "get_chat_id"
private const val approve = "approve"
private const val reject = "reject"

suspend fun main() {
    val bot = telegramBot(System.getenv("TOKEN"))
    val behaviour = bot.buildBehaviour(
        defaultExceptionsHandler = {
            it.printStackTrace()
        }
    ) {
        onCommand(start, scenarioReceiver = start())
        onCommand(proposePoll, scenarioReceiver = proposePoll())
        onCommand(getChatId, scenarioReceiver = getChatId())
        onCommandWithArgs(approve, scenarioReceiver = approve())
        onCommandWithArgs(reject, scenarioReceiver = reject())

        setMyCommands(
            BotCommand(start, start),
            BotCommand(proposePoll, proposePoll),
            BotCommand(getChatId, getChatId),
            BotCommand(approve, approve),
            BotCommand(reject, reject)
        )
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
