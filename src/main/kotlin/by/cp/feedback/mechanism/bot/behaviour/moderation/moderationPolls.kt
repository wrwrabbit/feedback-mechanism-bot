package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun moderationPolls(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    if (message.chat.id == moderatorsChatId.toChatId()) {
        val polls = PollRepository.getByStatus(PollStatus.ON_MODERATOR_REVIEW)
        if (polls.isEmpty()) {
            reply(message, emptyPollsMessage())
        } else {
            polls.forEach { poll ->
                execute(
                    SendTextMessage(
                        moderatorsChatId.toChatId(),
                        poll.toStatusMessage(),
                        replyMarkup = showModerationMarkup(poll.id)
                    )
                )
            }
        }
    }
}

fun emptyPollsMessage(): String = "Нет вопросов для модерации"

fun PollDto.toStatusMessage(): String = "Опрос #$id," + "\n" +
        "Вопрос $question," + "\n" +
        "Статус: ${status.toMessage()}" + "\n"
