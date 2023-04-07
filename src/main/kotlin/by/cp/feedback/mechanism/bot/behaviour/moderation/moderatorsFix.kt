package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.exception.CantRejectRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.utils.extensions.sameThread
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

fun moderatorFix(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(moderatorFixDC.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantRejectRejectedException()
    val langCode = "ru"
    val fixedPoll = waitTextMessage(
        SendTextMessage(moderatorsChatId.toChatId(), "Отправьте исправленный опрос в формате\n${pollTemplateText(langCode)}")
    ).filter { msg -> msg.sameThread(moderatorsChatId.toChatId()) }.first().content.text
    execute(
        SendTextMessage(
            poll.userId.toChatId(),
            "Модераторы предложили другую версию вашего опроса:\n$fixedPoll",
            replyMarkup = userModerationReviewMarkup(id, fixedPoll)
        )
    )
    execute(SendTextMessage(moderatorsChatId.toChatId(), "Вы предложили исправленную версию опроса"))
    delete((callback as MessageDataCallbackQuery).message)
}

fun pollTemplateText(langCode: String) = when (langCode) {
    "be" -> "${question(langCode)}: Колькі?\n" +
        "${answer(langCode)}: 10\n" +
        "...\n" +
        "${answer(langCode)}: 12\n" +
        "${moreThanOneAnswer(langCode)}: Так"

    else -> "${question(langCode)}: Сколько?\n" +
        "${answer(langCode)}: 10\n" +
        "...\n" +
        "${answer(langCode)}: 12\n" +
        "${moreThanOneAnswer(langCode)}: Да"
}
