package by.cp.feedback.mechanism.bot.behaviour.review

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
import by.cp.feedback.mechanism.bot.repository.PollVoteRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userApproveDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    PollRepository.addUserApprove(id)
    if (poll.userApproves + 1 == usersApprovalsRequired) {
        PollUserReviewRepository.delete(poll.id)
        PollRepository.start(poll.id)
        PollVoteRepository.save(poll.id)
        PollUserVoteRepository.save(poll.id)
        val message1 = execute(
            SendTextMessage(
                postChatId.toChatId(), PollVoteDto(
                    id = poll.id,
                    question = poll.question,
                    allowMultipleAnswers = poll.allowMultipleAnswers,
                    createdAt = poll.createdAt,
                    startedAt = null,
                    finishedAt = null,
                    options = poll.options,
                    results = poll.options.map { 0 }).toMessage()
            )
        )
        PollRepository.updateMessageId(id, message1.messageId)
        execute(
            SendTextMessage(
                poll.userId.toChatId(),
                sentToUsersVoteText()
            )
        )
    }
    delete((callback as MessageDataCallbackQuery).message)
}

fun sentToUsersVoteText() = "Ваш опрос отправлен на голосование пользователям"
