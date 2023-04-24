package by.cp.feedback.mechanism.bot.behaviour.utils

import by.cp.feedback.mechanism.bot.exception.FeedbackBotException
import by.cp.feedback.mechanism.bot.model.menuMarkup
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun tryF(extracted: suspend BehaviourContext.(message: CommonMessage<TextContent>) -> Unit): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message ->
        try {
            extracted(message)
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }

fun tryFUser(extracted: suspend BehaviourContext.(message: CommonMessage<TextContent>) -> Unit): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
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
