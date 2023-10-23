package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.asContentMessage
import dev.inmo.tgbotapi.extensions.utils.asTextContent
import dev.inmo.tgbotapi.extensions.utils.commonMessageOrNull
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.MarkdownParseMode
import dev.inmo.tgbotapi.types.message.MarkdownV2ParseMode
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun contactAdministrationInit(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val lastUserPollId = PollRepository.getLastPollByUserId(userId)?.id
    if (lastUserPollId != null) {
        reply(
            message,
            contactAdministrationReplyText
        )
    } else {
        reply(message, "Создайте хотя бы один опрос для связи с администрацией")
    }
}

fun contactAdministration(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val lastUserPollId = PollRepository.getLastPollByUserId(userId)?.id
    execute(
        SendTextMessage(
            moderatorsChatId.toChatId(),
            "*${contactAdministrationUserStartText}\\#${lastUserPollId}:*\n\n" + message.text + "\n\n" + contactAdministrationReplyText,
            parseMode = MarkdownV2ParseMode
        )
    )
    reply(message, contactAdministrationMessageSentText)
}

fun contactAdministrationReply(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val text = message.replyTo?.commonMessageOrNull()?.asContentMessage()?.content?.asTextContent()?.text!!
    val lastUserPollId = text.substringAfter("#").substringBefore(":").toLong()
    val userId = PollRepository.getUserIdByPollId(lastUserPollId)
    execute(
        SendTextMessage(
            userId.toChatId(),
            "*${contactAdministrationModerStartText}*\n\n" + message.text + "\n\n" + contactAdministrationReplyText,
            parseMode = MarkdownV2ParseMode
        )
    )
    reply(message, contactAdministrationMessageSentText)
}
