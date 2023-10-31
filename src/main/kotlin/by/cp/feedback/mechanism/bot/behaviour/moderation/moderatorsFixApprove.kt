package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.executeIfNotMuted
import by.cp.feedback.mechanism.bot.behaviour.utils.tryFModerators
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.moderatorFixApproveDC
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.model.userModerationReviewMarkup
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun moderatorFixApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = tryFModerators { callback ->
    val (originalMessageId, pollId) = callback.data.substring(moderatorFixApproveDC.length).split("_")
        .let { it[0].toLong() to it[1].toLong() }
    val poll = PollRepository.getById(pollId)
    val message = (callback as MessageDataCallbackQuery).message
    val fixedPoll = (callback.message.content as TextContent).text.let { text ->
        text.substring(text.indexOf("\n") + 1)
    }
    poll.userId?.let { pollUserId ->
        executeIfNotMuted(
            pollUserId,
            SendTextMessage(
                pollUserId.toChatId(),
                "Модераторы предложили другую версию вашего опроса:\n$fixedPoll",
                replyMarkup = userModerationReviewMarkup(pollId, fixedPoll)
            )
        )
    }
    execute(SendTextMessage(moderatorsChatId.toChatId(), "Вы предложили исправленную версию опроса #${poll.id}"))
    edit(
        chatId = moderatorsChatId.toChatId(),
        messageId = originalMessageId,
        text = "ИСПРАВЛЕНО\n$fixedPoll",
        replyMarkup = null
    )
    delete(message)
}
