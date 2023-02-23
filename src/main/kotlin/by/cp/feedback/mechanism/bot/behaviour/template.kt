package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.model.answer
import by.cp.feedback.mechanism.bot.model.langCode
import by.cp.feedback.mechanism.bot.model.moreThanOneAnswer
import by.cp.feedback.mechanism.bot.model.question
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun template(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    reply(message, pollTemplateText(message.langCode()))
}

fun pollTemplateText(langCode: String) = when (langCode) {
    "be" -> "${question(langCode)}: Колькі?\n" +
        "${answer(langCode)}: 10\n" +
        "...\n" +
        "${answer(langCode)}: 12\n" +
        "${moreThanOneAnswer(langCode)}: Так"

    else -> "${question(langCode)}: Сколько?\n" +
        "${answer(langCode)}: 10\n" +
        "...\n" +
        "${answer(langCode)}: 12\n" +
        "${moreThanOneAnswer(langCode)}: Да"
}
