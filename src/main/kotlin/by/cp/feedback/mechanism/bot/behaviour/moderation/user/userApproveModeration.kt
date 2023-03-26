package by.cp.feedback.mechanism.bot.behaviour.moderation.user

import by.cp.feedback.mechanism.bot.behaviour.moderation.sentToUsersReviewText
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.parsePoll
import by.cp.feedback.mechanism.bot.model.userApproveModerationDC
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userApproveModeration(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val (id, fixedPoll) = callback.data.substring(userApproveModerationDC.length).split("_")
        .let { it[1].toLong() to it[2] }
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    val langCode = "ru"
    val (question, options, allowMultipleAnswers) = parsePoll(fixedPoll, langCode)
    PollRepository.updatePoll(id, question, options, allowMultipleAnswers)
    PollRepository.updateStatus(poll.id, PollStatus.ON_USER_REVIEW)
    PollUserReviewRepository.save(poll.id)
    execute(SendTextMessage(poll.userId.toChatId(), sentToUsersReviewText(langCode)))
}
