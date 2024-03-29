package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.IdNotProvidedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.exception.YouAreNotOwnerOfPollException
import by.cp.feedback.mechanism.bot.model.toModeratorsMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun getPoll(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit = tryF { message, args ->
    if (args.size != 1) throw IdNotProvidedException()
    val id = args.first().toLong()
    val poll = PollRepository.getById(id)
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    if (userId != poll.userId) throw YouAreNotOwnerOfPollException()
    reply(message, poll.toModeratorsMessage())
}