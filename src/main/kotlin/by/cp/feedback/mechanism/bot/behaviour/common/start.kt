package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.menuMarkup
import by.cp.feedback.mechanism.bot.repository.PollUserReviewQueueRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteQueueRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun start(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    if (!UserRepository.exists(userId)) {
        UserRepository.save(userId, "ru")
        PollUserReviewQueueRepository.saveByUserId(userId)
        PollUserVoteQueueRepository.saveByUserId(userId)
    }
    reply(message, helloText(), replyMarkup = menuMarkup())
}

fun helloText() =
    """Создайте опрос, нажав на кнопку "✍️ создать опрос" - иконка Опрос внизу; или через меню три точки в правом верхнем углу чата. Опрос будет анонимным."""
