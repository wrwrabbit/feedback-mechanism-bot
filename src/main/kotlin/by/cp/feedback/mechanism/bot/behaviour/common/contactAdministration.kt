package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.asCallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.extensions.utils.commonMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun contactAdministrationInit(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val lastUserPollId = PollRepository.getLastPollByUserId(userId)?.id
    if (lastUserPollId != null) {
        reply(
            message,
            "Отправьте ваше сообщение для администрации в ответ на это сообщение",
            replyMarkup = contactAdministrationInitMarkup(lastUserPollId)
        )
    } else {
        reply(message, "Создайте хотя бы один опрос для связи с администрацией")
    }
}

fun contactAdministration(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val callbackData = message.replyTo?.commonMessageOrNull()?.replyMarkup?.keyboard?.firstOrNull()?.firstOrNull()
        ?.asCallbackDataInlineKeyboardButton()?.callbackData!!
    val lastUserPollId = callbackData.substring(contactAdministrationInitDC.length).toLong()
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            "Сообщение от создателя опроса #${lastUserPollId} \n" + message.text,
            replyMarkup = contactAdministrationMarkup(lastUserPollId)
        )
    )
    reply(message, "Ваше сообщение отправлено администрации администрацией")
}

fun contactAdministrationReply(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val callbackData = message.replyTo?.commonMessageOrNull()?.replyMarkup?.keyboard?.firstOrNull()?.firstOrNull()
        ?.asCallbackDataInlineKeyboardButton()?.callbackData!!
    val lastUserPollId = callbackData.substring(contactAdministrationDC.length).toLong()
    val userId = PollRepository.getUserIdByPollId(lastUserPollId)
    execute(
        SendTextMessage(
            userId.toChatId(),
            "Сообщение от администрации\n" + message.text,
            replyMarkup = contactAdministrationInitMarkup(lastUserPollId)
        )
    )
    reply(message, "Вы отправили сообщение пользователю")
}
