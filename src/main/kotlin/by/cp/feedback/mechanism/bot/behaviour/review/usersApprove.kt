package by.cp.feedback.mechanism.bot.behaviour.review

import by.cp.feedback.mechanism.bot.behaviour.utils.botLinkMarkup
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
        val message = execute(
            SendTextMessage(
                chatId = postChatId.toChatId(),
                text = PollVoteDto(
                    id = poll.id,
                    question = poll.question,
                    allowMultipleAnswers = poll.allowMultipleAnswers,
                    createdAt = poll.createdAt,
                    startedAt = null,
                    finishedAt = null,
                    options = poll.options,
                    results = poll.options.map { 0 }).toMessage(),
                replyMarkup = botLinkMarkup()
            ),
        )
        PollUserReviewRepository.delete(poll.id)
        PollRepository.start(poll.id, message.messageId)
        PollVoteRepository.save(poll.id)
        PollUserVoteRepository.save(poll.id)
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
