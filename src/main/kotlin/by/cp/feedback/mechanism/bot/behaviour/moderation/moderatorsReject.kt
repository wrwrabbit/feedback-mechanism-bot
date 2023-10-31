package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.executeIfNotMuted
import by.cp.feedback.mechanism.bot.exception.CantRejectRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.moderatorRejectDC
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.model.sentToUsersVoteText
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

fun moderatorReject(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(moderatorRejectDC.length).toLong()
    val poll = PollRepository.getById(id)
    if (poll.rejectionReason != null) throw CantRejectRejectedException()
    val rejectionReason = waitTextMessage(
        SendTextMessage(moderatorsChatId.toChatId(), "Отправьте причину отклонения в ответ на это сообщение")
    ).filter { msg -> msg.sameThread(moderatorsChatId.toChatId()) }.first().content.text
    PollRepository.updateRejectionReason(id, rejectionReason)
    PollRepository.updateStatus(poll.id, PollStatus.REJECTED)
    poll.userId?.let { pollUserId ->
        executeIfNotMuted(
            pollUserId,
            SendTextMessage(pollUserId.toChatId(), yourPollRejectedText(poll.id, rejectionReason))
        )
    }
    execute(SendTextMessage(moderatorsChatId.toChatId(), "Вы отклонили опрос"))
    val message = (callback as MessageDataCallbackQuery).message
    val text = (callback.message.content as TextContent).text
    edit(
        chatId = message.chat.id,
        messageId = message.messageId,
        text = "ОТКЛОНЕНО\n$text",
        replyMarkup = null
    )
}

fun yourPollRejectedText(pollId: Long, rejectionReason: String) =
    "Ваш опрос #$pollId отклонён. Причина: $rejectionReason"
