package by.cp.feedback.mechanism.bot.behaviour.vote

import by.cp.feedback.mechanism.bot.model.crossEmoji
import by.cp.feedback.mechanism.bot.model.tickEmoji
import by.cp.feedback.mechanism.bot.model.userVoteCheckAnswerDC
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.reply_markup
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userVoteCheckAnswer(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val (pollId, index) = callback.data.substring(userVoteCheckAnswerDC.length).split("_")
        .let { it[0].toLong() to it[1].toInt() }
    val message = (callback as MessageDataCallbackQuery).message
    val matrix = message.reply_markup!!.keyboard.mapIndexed { ix, buttons ->
        buttons.map { button ->
            val b = button as CallbackDataInlineKeyboardButton
            if (index == ix + 1) {
                val newText = if (b.text.contains(crossEmoji)) {
                    b.text.replace(crossEmoji, tickEmoji)
                } else {
                    b.text.replace(tickEmoji, crossEmoji)
                }
                CallbackDataInlineKeyboardButton(newText, b.callbackData)
            } else {
                CallbackDataInlineKeyboardButton(b.text, b.callbackData)
            }
        }
    }
    edit(message.chat, message.messageId, InlineKeyboardMarkup(matrix))
}
