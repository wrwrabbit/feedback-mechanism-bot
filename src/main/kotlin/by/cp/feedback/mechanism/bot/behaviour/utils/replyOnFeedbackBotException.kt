package by.cp.feedback.mechanism.bot.behaviour.utils

import by.cp.feedback.mechanism.bot.exception.FeedbackBotException
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun tryF(extracted: suspend BehaviourContext.(message: CommonMessage<TextContent>) -> Unit): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit =
    { message: CommonMessage<TextContent> ->
        try {
            extracted(message)
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }

fun tryF(extracted: suspend BehaviourContext.(message: CommonMessage<TextContent>, args: Array<String>) -> Unit): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit =
    { message: CommonMessage<TextContent>, args: Array<String> ->
        try {
            extracted(message, args)
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }
