package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.NotModeratorsChatException
import by.cp.feedback.mechanism.bot.exception.NotOneArgException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.exception.PollNotRejectedException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.langCode
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun unreject(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit = tryF { message, args ->
    if (message.chat.id.chatId != moderatorsChatId) throw NotModeratorsChatException()
    if (args.size != 1) throw NotOneArgException()
    val id = args.first().toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason == null) throw PollNotRejectedException()
    PollRepository.updateRejectionReason(id, null)
    PollRepository.updateStatus(poll.id, PollStatus.ON_MODERATOR_REVIEW)
    execute(SendTextMessage(poll.userId.toChatId(), yourPollUnRejectedText(poll.id, message.langCode())))
    reply(message, "You unrejected poll #${poll.id}")
}

fun yourPollUnRejectedText(pollId: Long, langCode: String) = when (langCode) {
    "be" -> "Ваша апытанне #$pollId прынята"
    else -> "Ваш опрос #$pollId принято"
}
