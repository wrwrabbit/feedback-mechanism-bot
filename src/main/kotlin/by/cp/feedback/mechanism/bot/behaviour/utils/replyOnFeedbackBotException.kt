package by.cp.feedback.mechanism.bot.behaviour.utils

import by.cp.feedback.mechanism.bot.exception.FeedbackBotException
import by.cp.feedback.mechanism.bot.model.UserStatus
import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.menuMarkup
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.abstracts.Request
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.PollContent
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

suspend fun BehaviourContext.executeIfNotMuted(userId: Long?, request: Request<*>) {
    if (userId != null && UserRepository.getById(userId)?.status == UserStatus.UNMUTED) {
        execute(request)
    }
}

suspend fun executeIfNotMuted(userId: Long?, request: Request<*>) {
    if (userId != null && UserRepository.getById(userId)?.status == UserStatus.UNMUTED) {
        bot.execute(request)
    }
}

fun tryF(extracted: suspend BehaviourContext.(message: CommonMessage<TextContent>) -> Unit): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message ->
        try {
            extracted(message)
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }

fun tryFUser(extracted: suspend BehaviourContext.(message: CommonMessage<PollContent>) -> Unit): suspend BehaviourContext.(CommonMessage<PollContent>) -> Unit =
    { message ->
        try {
            extracted(message)
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message, replyMarkup = menuMarkup())
        }
    }

fun tryF(extracted: suspend BehaviourContext.(message: CommonMessage<TextContent>, args: Array<String>) -> Unit): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit =
    { message, args ->
        try {
            extracted(message, args)
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }

fun tryFModerators(extracted: suspend BehaviourContext.(callback: DataCallbackQuery) -> Unit): suspend BehaviourContext.(DataCallbackQuery) -> Unit =
    { callback ->
        try {
            extracted(callback)
        } catch (exception: FeedbackBotException) {
            reply(
                (callback as MessageDataCallbackQuery).message,
                "${exception.message}\nНажмите ещё раз на кнопку редактировать"
            )
        }
    }
