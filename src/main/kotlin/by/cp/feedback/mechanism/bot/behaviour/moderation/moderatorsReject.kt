package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.CantRejectRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.moderatorApproveDC
import by.cp.feedback.mechanism.bot.model.moderatorRejectDC
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.first

fun moderatorReject(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(moderatorRejectDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantRejectRejectedException()
    val chatId: Long = callback.from.id.chatId
    val langCode = "ru"
    val rejectionReason = waitTextMessage(
        SendTextMessage(moderatorsChatId.toChatId(), "Отправьте причину отклонения")
    ).first().content.text
    PollRepository.updateRejectionReason(id, rejectionReason)
    PollRepository.updateStatus(poll.id, PollStatus.REJECTED)
    execute(SendTextMessage(poll.userId.toChatId(), yourPollRejectedText(poll.id, langCode, rejectionReason)))
    execute(SendTextMessage(moderatorsChatId.toChatId(), "Вы отклонили опрос"))
    delete((callback as MessageDataCallbackQuery).message)
}

fun yourPollRejectedText(pollId: Long, langCode: String, rejectionReason: String) = when (langCode) {
    "be" -> "Ваша апытанне #$pollId адмоўлена"
    else -> "Ваш опрос #$pollId отклонён. Причина: $rejectionReason"
}
