package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.model.pollTemplate
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun template(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    reply(message, pollTemplate)
}
