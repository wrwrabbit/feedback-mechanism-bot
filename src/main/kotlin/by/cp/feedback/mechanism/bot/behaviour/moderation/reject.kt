package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.CantRejectRejectedException
import by.cp.feedback.mechanism.bot.exception.NotModeratorsChatException
import by.cp.feedback.mechanism.bot.exception.NotTwoArgException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun reject(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit = tryF { message, args ->
    if (message.chat.id.chatId != moderatorsChatId) throw NotModeratorsChatException()
    if (args.size != 2) throw NotTwoArgException()
    val id = args.first().toLong()
    val rejectionReason = args[1]
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantRejectRejectedException()
    PollRepository.updateRejectionReason(id, rejectionReason)
    PollRepository.updateStatus(poll.id, PollStatus.REJECTED)
    val langCode = UserRepository.langCodeById(poll.userId)
    execute(SendTextMessage(poll.userId.toChatId(), yourPollRejectedText(poll.id, langCode)))
    reply(message, "You rejected poll #${poll.id}")
}

fun yourPollRejectedText(pollId: Long, langCode: String) = when (langCode) {
    "be" -> "Ваша апытанне #$pollId адмоўлена"
    else -> "Ваш опрос #$pollId отклонён"
}
