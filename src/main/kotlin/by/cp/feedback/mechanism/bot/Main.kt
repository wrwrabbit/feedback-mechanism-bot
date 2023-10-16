package by.cp.feedback.mechanism.bot

import by.cp.feedback.mechanism.bot.behaviour.common.*
import by.cp.feedback.mechanism.bot.behaviour.moderation.*
import by.cp.feedback.mechanism.bot.behaviour.review.userApprove
import by.cp.feedback.mechanism.bot.behaviour.review.userUnApprove
import by.cp.feedback.mechanism.bot.behaviour.vote.userVote
import by.cp.feedback.mechanism.bot.behaviour.vote.userVoteCheckAnswer
import by.cp.feedback.mechanism.bot.behaviour.vote.userVoteMultipleAnswers
import by.cp.feedback.mechanism.bot.exception.FeedbackBotException
import by.cp.feedback.mechanism.bot.model.*
import dev.inmo.tgbotapi.bot.exceptions.CommonRequestException
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.*
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import dev.inmo.tgbotapi.types.BotCommand
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CancellationException
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FeedbackMechanismBot

const val startCommand = "start"
const val moderationPollsCommand = "moderation_polls"
const val getChatIdCommand = "get_my_id"
const val myPollsCommand = "my_polls"
const val getPollCommand = "get_poll"
const val muteCommand = "mute"
const val unmuteCommand = "unmute"
const val deleteCommand = "delete"

private val logger = KotlinLogging.logger { }

suspend fun main(args: Array<String>) {
    val behaviour = bot.buildBehaviour(
        defaultExceptionsHandler = {
            when {
                it is CancellationException -> {}
                it is FeedbackBotException -> {}
                it is ConnectTimeoutException -> logger.error { "Connect timeout has expired" }
                it is HttpRequestTimeoutException -> logger.error { "Connect timeout has expired" }
                (it is CommonRequestException) &&
                        (it.message?.contains("message to delete not found") ?: false) ->{}
                (it is CommonRequestException) &&
                        (it.message?.contains("bot was blocked by the user") ?: false) ->{}
                (it is CommonRequestException) &&
                        (it.message?.contains("message is not modified") ?: false) ->{}
                else -> {
                    logger.error(it) { "Exception in bot" }
                }
            }
        }
    ) {
        //COMMON
        onCommand(getChatIdCommand, scenarioReceiver = getChatId())
        onCommandWithArgs(getPollCommand, scenarioReceiver = getPoll())
        onCommandWithArgs(startCommand, scenarioReceiver = start())
        onCommand(myPollsCommand, scenarioReceiver = myPolls())
        onCommand(muteCommand, scenarioReceiver = mute())
        onCommand(unmuteCommand, scenarioReceiver = unmute())
        onCommand(deleteCommand, scenarioReceiver = delete())
        onDataCallbackQuery(Regex("$myPollsDC.*"), scenarioReceiver = myPollsDC())
        onText(initialFilter = { it.content.text == "\uD83D\uDDC2 мои опросы" }, scenarioReceiver = myPolls())
        //MODERATION
        onCommand(moderationPollsCommand, scenarioReceiver = moderationPolls())
        onDataCallbackQuery(Regex("$showModerationDC\\d*"), scenarioReceiver = showModeration())
        onDataCallbackQuery(Regex("$moderatorRejectDC.*"), scenarioReceiver = moderatorReject())
        onDataCallbackQuery(Regex("$moderatorApproveDC\\d*"), scenarioReceiver = moderatorApprove())
        onDataCallbackQuery(Regex("$moderatorFixDC.*"), scenarioReceiver = moderatorFix())
        onDataCallbackQuery(Regex("$moderatorFixApproveDC.*"), scenarioReceiver = moderatorFixApprove())
        onDataCallbackQuery(Regex("$moderatorFixRejectDC.*"), scenarioReceiver = moderatorFixReject())
        onPoll(scenarioReceiver = userProposePoll())
        onDataCallbackQuery(Regex("$userApproveModerationDC.*"), scenarioReceiver = userApproveModeration())
        onDataCallbackQuery(Regex("$userRejectModerationDC.*"), scenarioReceiver = userRejectModeration())
        //REVIEW
        onDataCallbackQuery(Regex("$userApproveDC\\d*"), scenarioReceiver = userApprove())
        onDataCallbackQuery(Regex("$userUnApproveDC\\d*"), scenarioReceiver = userUnApprove())
        //VOTE
        onDataCallbackQuery(Regex("$userVoteDC.*"), scenarioReceiver = userVote())
        onDataCallbackQuery(
            Regex("$userVoteMultipleAnswersDC.*"),
            scenarioReceiver = userVoteMultipleAnswers()
        )
        onDataCallbackQuery(Regex("$userVoteCheckAnswerDC.*"), scenarioReceiver = userVoteCheckAnswer())

        setMyCommands(
            BotCommand(startCommand, "Пример: /start"),
            BotCommand(getChatIdCommand, "Пример: /get_my_id"),
            BotCommand(getPollCommand, "Пример: /get_poll 20"),
            BotCommand(myPollsCommand, "Пример: /my_polls"),
            BotCommand(muteCommand, "Пример: /mute"),
            BotCommand(unmuteCommand, "Пример: /unmute"),
            BotCommand(deleteCommand, "Пример: /delete"),
        )
    }
    runApplication<FeedbackMechanismBot>(*args)
    if (System.getenv("WEBHOOK_ROUTE").isNullOrBlank() || System.getenv("WEBHOOK_URL").isNullOrBlank()) {
        bot.startGettingOfUpdatesByLongPolling(behaviour)
    } else {
        bot.setWebhookInfoAndStartListenWebhooks(
            listenPort = 8888,
            listenRoute = System.getenv("WEBHOOK_ROUTE"),
            engineFactory = Netty,
            setWebhookRequest = SetWebhook(url = System.getenv("WEBHOOK_URL")),
            exceptionsHandler = {
                it.printStackTrace()
            },
            block = behaviour.asUpdateReceiver
        )
    }
}
