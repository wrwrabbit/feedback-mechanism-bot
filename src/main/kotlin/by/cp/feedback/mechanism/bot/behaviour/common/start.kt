package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.MessageQueueType
import by.cp.feedback.mechanism.bot.model.menuMarkup
import by.cp.feedback.mechanism.bot.repository.MessageQueueRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit = tryF { message, args ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    if (!UserRepository.exists(userId)) {
        UserRepository.save(userId, "ru")
        MessageQueueRepository.saveReviewByUserId(userId, MessageQueueType.REVIEW)
        MessageQueueRepository.saveVoteByUserId(userId, MessageQueueType.VOTE)
    } else if (args.isNotEmpty()) {
        val pollId = args[0].toLong()
        MessageQueueRepository.save(userId = userId, pollId = pollId, type = MessageQueueType.VOTE)
    }
    reply(message, helloText(), replyMarkup = menuMarkup())
}

fun helloText() =
    """Создайте опрос, нажав на кнопку "✍️ создать опрос" - иконка Опрос внизу; или через меню три точки в правом верхнем углу чата. Опрос будет анонимным."""
