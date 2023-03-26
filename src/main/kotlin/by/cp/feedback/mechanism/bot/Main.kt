package by.cp.feedback.mechanism.bot

import by.cp.feedback.mechanism.bot.behaviour.common.getChatId
import by.cp.feedback.mechanism.bot.behaviour.common.getPoll
import by.cp.feedback.mechanism.bot.behaviour.common.myPolls
import by.cp.feedback.mechanism.bot.behaviour.common.start
import by.cp.feedback.mechanism.bot.behaviour.moderation.moderatorApprove
import by.cp.feedback.mechanism.bot.behaviour.moderation.moderatorFix
import by.cp.feedback.mechanism.bot.behaviour.moderation.moderatorReject
import by.cp.feedback.mechanism.bot.behaviour.moderation.user.proposePoll
import by.cp.feedback.mechanism.bot.behaviour.moderation.user.userApproveModeration
import by.cp.feedback.mechanism.bot.behaviour.moderation.user.userRejectModeration
import by.cp.feedback.mechanism.bot.behaviour.review.userApprove
import by.cp.feedback.mechanism.bot.behaviour.review.userUnApprove
import by.cp.feedback.mechanism.bot.behaviour.vote.userVote
import by.cp.feedback.mechanism.bot.behaviour.vote.userVoteCheckAnswer
import by.cp.feedback.mechanism.bot.behaviour.vote.userVoteMultipleAnswers
import by.cp.feedback.mechanism.bot.model.*
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.setWebhookInfoAndStartListenWebhooks
import dev.inmo.tgbotapi.requests.webhook.SetWebhook
import dev.inmo.tgbotapi.types.BotCommand
import io.ktor.server.netty.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FeedbackMechanismBot

const val startCommand = "start"
const val proposePollCommand = "propose_poll"
const val getChatIdCommand = "get_chat_id"
const val myPollsCommand = "my_polls"
const val getPollCommand = "get_poll"

suspend fun main(args: Array<String>) {
    val behaviour = bot.buildBehaviour(
        defaultExceptionsHandler = {
            it.printStackTrace()
        }
    ) {
        //COMMON
        onCommand(getChatIdCommand, scenarioReceiver = getChatId())
        onCommandWithArgs(getPollCommand, scenarioReceiver = getPoll())
        onCommand(startCommand, scenarioReceiver = start())
        onCommand(myPollsCommand, scenarioReceiver = myPolls())
        onText(initialFilter = { it.content.text == "\uD83D\uDDC2 мои опросы" }, scenarioReceiver = myPolls())
        //MODERATION
        onCommand(proposePollCommand, scenarioReceiver = proposePoll())
        onText(initialFilter = { it.content.text == "✍️ создать опрос" }, scenarioReceiver = proposePoll())
        onDataCallbackQuery(Regex("$moderatorApproveDC\\d*"), scenarioReceiver = moderatorApprove())
        onDataCallbackQuery(Regex("$moderatorFixDC.*"), scenarioReceiver = moderatorFix())
        onDataCallbackQuery(Regex("$moderatorRejectDC.*"), scenarioReceiver = moderatorReject())
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
            BotCommand(startCommand, "startCommand"),
            BotCommand(proposePollCommand, "proposePollCommand"),
            BotCommand(getChatIdCommand, "getChatIdCommand"),
            BotCommand(getPollCommand, "getPollCommand"),
            BotCommand(myPollsCommand, "myPollsCommand")
        )
    }
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
    runApplication<FeedbackMechanismBot>(*args)
}
