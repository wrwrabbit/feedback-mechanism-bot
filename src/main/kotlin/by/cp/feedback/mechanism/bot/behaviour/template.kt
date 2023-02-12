package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

private const val template = "Вопрос: Сколько?\n" +
    "Ответ: 10\n" +
    "...\n" +
    "Ответ: 12\n" +
    "Больше одного ответа: Да"

fun template(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    reply(message, template)
}
