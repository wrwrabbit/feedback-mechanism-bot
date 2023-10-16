package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.tryFModerators
import by.cp.feedback.mechanism.bot.exception.CantRejectRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

fun moderatorFix(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = tryFModerators { callback ->
    val id = callback.data.substring(moderatorFixDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantRejectRejectedException()
    val fixedPoll = waitTextMessage(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            "Отправьте исправленный опрос (в ответе на это сообщение) в формате\n${pollTemplateText()}"
        )
    ).filter { msg -> msg.sameThread(moderatorsChatId.toChatId()) }.first().content.text
    parsePoll(fixedPoll)
    val message = (callback as MessageDataCallbackQuery).message
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            "Ваша исправленная версия опроса #${poll.id}:\n$fixedPoll",
            replyMarkup = moderatorsFixMarkup(message.messageId, id)
        )
    )
}

fun pollTemplateText() = "${question()}: Сколько?\n" +
        "${answer()}: 10\n" +
        "...\n" +
        "${answer()}: 12\n" +
        "${moreThanOneAnswer()}: Да"
