package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.CantRejectRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.moderatorRejectDC
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

fun moderatorReject(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(moderatorRejectDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantRejectRejectedException()
    val rejectionReason = waitTextMessage(
        SendTextMessage(moderatorsChatId.toChatId(), "Отправьте причину отклонения")
    ).filter { msg -> msg.sameThread(moderatorsChatId.toChatId()) }.first().content.text
    PollRepository.updateRejectionReason(id, rejectionReason)
    PollRepository.updateStatus(poll.id, PollStatus.REJECTED)
    execute(SendTextMessage(poll.userId.toChatId(), yourPollRejectedText(poll.id, rejectionReason)))
    execute(SendTextMessage(moderatorsChatId.toChatId(), "Вы отклонили опрос"))
    val message = (callback as MessageDataCallbackQuery).message
    edit(
        message.chat,
        message.messageId,
        InlineKeyboardMarkup(matrix {
            row {
                +CallbackDataInlineKeyboardButton(
                    "Rejected",
                    callbackData = "xxxxxxxxxx"
                )
            }
        })
    )
}

fun yourPollRejectedText(pollId: Long, rejectionReason: String) =
    "Ваш опрос #$pollId отклонён. Причина: $rejectionReason"
