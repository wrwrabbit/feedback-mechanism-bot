package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.myPollsDC
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row

fun myPolls(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val polls = PollRepository.getByUserId(userId, 1)
    val pollsResponse = polls.content.joinToString("\n") { it.toStatusMessage() }
    val response = pollsResponse.ifEmpty { emptyPollsMessage() }
    reply(message, response, replyMarkup = pollsMarkup(userId, polls.page, polls.totalPages, polls.totalElements))
}

fun myPollsDC(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val (userId, page) = callback.data.substring(myPollsDC.length).split("_")
        .let { it[0].toLong() to it[1].toInt() }
    val polls = PollRepository.getByUserId(userId, page)
    val pollsResponse = polls.content.joinToString("\n") { it.toStatusMessage() }
    val response = pollsResponse.ifEmpty { emptyPollsMessage() }
    val message = (callback as MessageDataCallbackQuery).message
    edit(
        message.chat.id, message.messageId, response, replyMarkup = pollsMarkup(
            userId,
            polls.page,
            polls.totalPages,
            polls.totalElements
        )
    )
}

fun pollsMarkup(userId: Long, page: Int, totalPages: Int, totalElements: Long) =
    if (totalElements > 0L && totalPages > 1) {
        InlineKeyboardMarkup(
            matrix {
                row {
                    if (page != 1) {
                        +CallbackDataInlineKeyboardButton(
                            "<<",
                            callbackData = "$myPollsDC${userId}_1"
                        )
                        +CallbackDataInlineKeyboardButton(
                            "<",
                            callbackData = "$myPollsDC${userId}_${page - 1}"
                        )
                    }
                    +CallbackDataInlineKeyboardButton(
                        page.toString(),
                        callbackData = "____________________"
                    )
                    if (page != totalPages) {
                        +CallbackDataInlineKeyboardButton(
                            ">",
                            callbackData = "$myPollsDC${userId}_${page + 1}"
                        )
                        +CallbackDataInlineKeyboardButton(
                            ">>",
                            callbackData = "$myPollsDC${userId}_${totalPages}"
                        )
                    }
                }
            }
        )
    } else {
        null
    }

fun emptyPollsMessage(): String = "У вас нет опросов"

fun PollDto.toStatusMessage(): String = "Опрос #$id" + "\n" +
        "Вопрос $question" + "\n" +
        "Статус: ${status.toMessage()}" + "\n" +
        if (status == PollStatus.REJECTED && rejectionReason != null) "Причина отказа: $rejectionReason\n" else ""
