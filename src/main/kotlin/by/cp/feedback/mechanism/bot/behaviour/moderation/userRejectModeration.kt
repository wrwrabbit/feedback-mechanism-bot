package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.userRejectModerationDC
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userRejectModeration(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userRejectModerationDC.length).toLong()
    val poll = PollRepository.getById(id)
    if (poll.status == PollStatus.ON_MODERATOR_REVIEW) {
        PollRepository.updateStatus(poll.id, PollStatus.REJECTED)
        val message = (callback as MessageDataCallbackQuery).message
        val text = (callback.message.content as TextContent).text
        edit(
            chatId = message.chat.id,
            messageId = message.messageId,
            text = "ОТКЛОНЕНО\n$text",
            replyMarkup = null
        )
    }
}
