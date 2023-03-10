package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.NotOneArgException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.exception.YouAreNotOwnerOfPollException
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun getPoll(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit = tryF { message, args ->
    if (args.size != 1) throw NotOneArgException()
    val id = args.first().toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val langCode = UserRepository.langCodeById(userId)
    if (userId != poll.userId) throw YouAreNotOwnerOfPollException()
    reply(message, poll.toMessage(langCode))
}