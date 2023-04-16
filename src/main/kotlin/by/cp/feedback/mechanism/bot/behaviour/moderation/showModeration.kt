package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.model.moderatorsReviewMarkup
import by.cp.feedback.mechanism.bot.model.showModerationDC
import by.cp.feedback.mechanism.bot.model.toModeratorsMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun showModeration(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(showModerationDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            poll.toModeratorsMessage(),
            replyMarkup = moderatorsReviewMarkup(id, poll.moderatorApproves.size)
        )
    )
}
