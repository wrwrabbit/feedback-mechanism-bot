package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.executeIfNotMuted
import by.cp.feedback.mechanism.bot.exception.AlreadyApprovedException
import by.cp.feedback.mechanism.bot.exception.CantApproveRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.MessageQueueRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun moderatorApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(moderatorApproveDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantApproveRejectedException()
    val userId = callback.user.id.chatId
    if (userId in poll.moderatorApproves) throw AlreadyApprovedException()
    val resultArray = poll.moderatorApproves.plus(userId)
    PollRepository.updateApproves(id, resultArray)
    if (resultArray.size == moderatorsApprovalsRequired) {
        PollRepository.updateStatus(poll.id, PollStatus.ON_USER_REVIEW)
        MessageQueueRepository.save(poll.id, MessageQueueType.REVIEW)
        executeIfNotMuted(poll.userId, SendTextMessage(userId.toChatId(), sentToUsersReviewText()))
        val message = (callback as MessageDataCallbackQuery).message
        val text = (callback.message.content as TextContent).text
        edit(
            chatId = message.chat.id,
            messageId = message.messageId,
            text = "УТВЕРЖДЕНО\n$text",
            replyMarkup = null
        )
    } else {
        val message = (callback as MessageDataCallbackQuery).message
        edit(message.chat, message.messageId, moderatorsReviewMarkup(poll.id, resultArray.size))
    }
}

fun sentToUsersReviewText() = "Ваш опрос отправлен на пересмотр пользователям"
