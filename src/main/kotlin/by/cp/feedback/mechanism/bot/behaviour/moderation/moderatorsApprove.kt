package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.AlreadyApprovedException
import by.cp.feedback.mechanism.bot.exception.CantApproveRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.moderatorApproveDC
import by.cp.feedback.mechanism.bot.model.moderatorsApprovalsRequired
import by.cp.feedback.mechanism.bot.model.moderatorsReviewMarkup
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row

fun moderatorApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(moderatorApproveDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantApproveRejectedException()
    val userId: Long = callback.user.id.chatId
    if (userId in poll.moderatorApproves) throw AlreadyApprovedException()
    val resultArray = poll.moderatorApproves.plus(userId)
    PollRepository.updateApproves(id, resultArray)
    if (resultArray.size == moderatorsApprovalsRequired) {
        PollRepository.updateStatus(poll.id, PollStatus.ON_USER_REVIEW)
        PollUserReviewRepository.save(poll.id)
        execute(SendTextMessage(poll.userId.toChatId(), sentToUsersReviewText()))
        val message = (callback as MessageDataCallbackQuery).message
        edit(
            message.chat,
            message.messageId,
            InlineKeyboardMarkup(matrix {
                row {
                    +CallbackDataInlineKeyboardButton(
                        "Approved",
                        callbackData = "xxxxxxxxxx"
                    )
                }
            })
        )
    } else {
        val message = (callback as MessageDataCallbackQuery).message
        edit(message.chat, message.messageId, moderatorsReviewMarkup(poll.id, resultArray.size))
    }
}

fun sentToUsersReviewText() = "Ваш опрос отправлен на пересмотр пользователям"
