package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.answer
import by.cp.feedback.mechanism.bot.model.moreThanOneAnswer
import by.cp.feedback.mechanism.bot.model.question
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun template(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    // TODO return on behaviour finish
//    val langCode = UserRepository.langCodeById(userId)
    val langCode = "ru"
    reply(message, pollTemplateText(langCode))
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
