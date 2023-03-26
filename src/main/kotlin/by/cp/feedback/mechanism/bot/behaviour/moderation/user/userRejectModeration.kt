package by.cp.feedback.mechanism.bot.behaviour.moderation.user

import by.cp.feedback.mechanism.bot.behaviour.moderation.sentToUsersReviewText
import by.cp.feedback.mechanism.bot.exception.AlreadyApprovedException
import by.cp.feedback.mechanism.bot.exception.CantApproveRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.model.moderatorApproveDC
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userRejectModeration(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userRejectModerationDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    PollRepository.updateStatus(poll.id, PollStatus.REJECTED)
    execute(SendTextMessage(poll.userId.toChatId(), "Вы отклонили вариант опроса"))
}
