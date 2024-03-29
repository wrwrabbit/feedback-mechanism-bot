package by.cp.feedback.mechanism.bot.behaviour.utils

import by.cp.feedback.mechanism.bot.model.bot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.utils.formatting.usernameLink
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.URLInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row

var botName: String? = null
var botId: Long? = null

private suspend fun botLink() = if (botName == null) {
    botName = bot.getMe().username.usernameLink
    botName
} else {
    botName
}!!

suspend fun botId() = if (botId == null) {
    botId = bot.getMe().id.chatId
    botId
} else {
    botId
}!!

suspend fun botLinkMarkup(pollId: Long) = InlineKeyboardMarkup(
    matrix {
        row {
            +URLInlineKeyboardButton("Проголосовать", url = botLink() + "?start=$pollId")
        }
    }
)