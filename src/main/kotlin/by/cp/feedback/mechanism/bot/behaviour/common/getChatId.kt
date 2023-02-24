package by.cp.feedback.mechanism.bot.behaviour.common

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun getChatId(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = { message ->
    reply(message, "ChatId={${message.chat.id.chatId}}")
}
