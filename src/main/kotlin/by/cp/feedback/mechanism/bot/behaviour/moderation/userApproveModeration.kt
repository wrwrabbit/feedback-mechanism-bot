package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.parsePoll
import by.cp.feedback.mechanism.bot.model.userApproveModerationDC
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.textContentOrThrow
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userApproveModeration(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userApproveModerationDC.length).toLong()
    val fixedPoll = (callback as MessageDataCallbackQuery).message.content.textContentOrThrow().text
        .split("\n").let { it.subList(1, it.size) }.joinToString("\n")
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    val (question, options, allowMultipleAnswers) = parsePoll(fixedPoll)
    PollRepository.updatePoll(id, question, options, allowMultipleAnswers)
    PollRepository.updateStatus(poll.id, PollStatus.ON_USER_REVIEW)
    PollUserReviewRepository.save(poll.id)
    execute(SendTextMessage(poll.userId.toChatId(), sentToUsersReviewText()))
}
