package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.AlreadyApprovedException
import by.cp.feedback.mechanism.bot.exception.CantApproveRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.moderatorApproveDataCallback
import by.cp.feedback.mechanism.bot.model.moderatorsApprovalsRequired
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
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
    val id = callback.data.substring(moderatorApproveDataCallback.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantApproveRejectedException()
    val userId: Long = callback.user.id.chatId
    val langCode = UserRepository.langCodeById(userId)
    if (userId in poll.moderatorApproves) throw AlreadyApprovedException()
    val resultArray = poll.moderatorApproves.plus(userId)
    PollRepository.updateApproves(id, resultArray)
    if (resultArray.size == moderatorsApprovalsRequired) {
        PollRepository.updateStatus(poll.id, PollStatus.READY_FOR_USER_REVIEW)
        execute(
            SendTextMessage(
                poll.userId.toChatId(),
                pollApprovedByModeratorsText(poll.id, langCode)
            )
        )
    }
    val message = (callback as MessageDataCallbackQuery).message
    val matrix = matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅ ${resultArray.size}/$moderatorsApprovalsRequired",
                callbackData = "$moderatorApproveDataCallback${poll.id}"
            )
        }
    }
    edit(message.chat, message.messageId, InlineKeyboardMarkup(matrix))
}

fun pollApprovedByModeratorsText(pollId: Long, langCode: String) = when (langCode) {
    "be" -> "Ваша апытанне #$pollId зацверджана мадэратарамі"
    else -> "Ваш опрос #$pollId утверждён модераторами"
}
