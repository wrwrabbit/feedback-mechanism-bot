package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.languageDataCallback
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
// TODO return on behaviour finish
//fun language(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
//    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
//    val langCode = UserRepository.langCodeById(userId)
//    reply(message, languageText(langCode), replyMarkup = InlineKeyboardMarkup(
//        matrix {
//            row {
//                +CallbackDataInlineKeyboardButton(
//                    "\uD83E\uDD0D❤️\uD83E\uDD0D",
//                    callbackData = "${languageDataCallback}be"
//                )
//                +CallbackDataInlineKeyboardButton(
//                    "\uD83E\uDD0D\uD83D\uDC99❤️",
//                    callbackData = "${languageDataCallback}ru"
//                )
//            }
//        }
//    ))
//}
//
//fun languageText(langCode: String) = when (langCode) {
//    "be" -> "Абярыце мову"
//    else -> "Выберете язык"
//}
