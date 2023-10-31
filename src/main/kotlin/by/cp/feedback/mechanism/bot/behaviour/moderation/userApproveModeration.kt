package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.MessageQueueType
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.parsePoll
import by.cp.feedback.mechanism.bot.model.userApproveModerationDC
import by.cp.feedback.mechanism.bot.repository.MessageQueueRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.textContentOrThrow
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userApproveModeration(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userApproveModerationDC.length).toLong()
    val poll = PollRepository.getById(id)
    if (poll.status == PollStatus.ON_MODERATOR_REVIEW) {
        val fixedPoll = (callback as MessageDataCallbackQuery).message.content.textContentOrThrow().text
            .split("\n").let { it.subList(1, it.size) }.joinToString("\n")
        val (question, options, allowMultipleAnswers) = parsePoll(fixedPoll)
        PollRepository.updatePoll(id, question, options, allowMultipleAnswers)
        PollRepository.updateStatus(poll.id, PollStatus.ON_USER_REVIEW)
        MessageQueueRepository.save(poll.id, MessageQueueType.REVIEW)
        val message = callback.message
        val text = (callback.message.content as TextContent).text
        edit(
            chatId = message.chat.id,
            messageId = message.messageId,
            text = "УТВЕРЖДЕНО\n$text",
            replyMarkup = null
        )
    }
}
