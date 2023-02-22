package by.cp.feedback.mechanism.bot

import by.cp.feedback.mechanism.bot.behaviour.*
import by.cp.feedback.mechanism.bot.model.*
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommandWithArgs
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onDataCallbackQuery
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

suspend fun main(args: Array<String>) {
    val behaviour = bot.buildBehaviour(
        defaultExceptionsHandler = {
            it.printStackTrace()
        }
    ) {
        onCommand(startCommand, scenarioReceiver = start())
        onCommand(sendToModeratorsReviewCommand, scenarioReceiver = sendToModeratorsReview())
        onCommand(getChatIdCommand, scenarioReceiver = getChatId())
        onDataCallbackQuery(Regex("$moderatorApproveDataCallback\\d*"), scenarioReceiver = moderatorApprove())
        onDataCallbackQuery(Regex("$userApproveDataCallback\\d*"), scenarioReceiver = userApprove())
        onDataCallbackQuery(Regex("$userUnApproveDataCallback\\d*"), scenarioReceiver = userUnApprove())
        onDataCallbackQuery(Regex("$userVoteDataCallback.*"), scenarioReceiver = userVote())
        onDataCallbackQuery(
            Regex("$userVoteMultipleAnswersDataCallback.*"),
            scenarioReceiver = userVoteMultipleAnswers()
        )
        onDataCallbackQuery(Regex("$userVoteCheckAnswerDataCallback.*"), scenarioReceiver = userVoteCheckAnswer())
        onCommandWithArgs(rejectCommand, scenarioReceiver = reject())
        onCommandWithArgs(fixPollCommand, scenarioReceiver = fixPoll())
        onCommandWithArgs(sendToUsersReviewCommand, scenarioReceiver = sendToUsersReview())
        onCommandWithArgs(sendToVoteCommand, scenarioReceiver = sendToVote())
        onCommandWithArgs(getPollCommand, scenarioReceiver = getPoll())
        onCommandWithArgs(unrejectCommand, scenarioReceiver = unreject())
        onCommand(myPollsCommand, scenarioReceiver = myPolls())
        onCommand(templateCommand, scenarioReceiver = template())

        setMyCommands(
            BotCommand(startCommand, startCommand),
            BotCommand(sendToModeratorsReviewCommand, sendToModeratorsReviewCommand),
            BotCommand(getChatIdCommand, getChatIdCommand),
            BotCommand(rejectCommand, rejectCommand),
            BotCommand(fixPollCommand, fixPollCommand),
            BotCommand(getPollCommand, getPollCommand),
            BotCommand(unrejectCommand, unrejectCommand),
            BotCommand(myPollsCommand, myPollsCommand),
            BotCommand(templateCommand, templateCommand),
            BotCommand(sendToUsersReviewCommand, sendToUsersReviewCommand),
            BotCommand(sendToVoteCommand, sendToVoteCommand),
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
    runApplication<FeedbackMechanismBot>(*args)
}
