package by.cp.feedback.mechanism.bot.behaviour.vote

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.NotOneArgException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.exception.YouAreNotOwnerOfPollException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.PollVoteDto
import by.cp.feedback.mechanism.bot.model.postChatId
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
import by.cp.feedback.mechanism.bot.repository.PollVoteRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun sendToVote(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit =
    tryF { message, args ->
        if (args.size != 1) throw NotOneArgException()
        val id = args.first().toLong()
        val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
        val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
        // TODO return on behaviour finish
//    val langCode = UserRepository.langCodeById(userId)
        val langCode = "ru"
        if (userId != poll.userId) throw YouAreNotOwnerOfPollException()
        PollRepository.updateStatus(poll.id, PollStatus.VOTING)
        PollVoteRepository.save(poll.id)
        PollUserVoteRepository.save(poll.id)
        val message1 = execute(
            SendTextMessage(
                postChatId.toChatId(), PollVoteDto(
                    id = poll.id,
                    question = poll.question,
                    allowMultipleAnswers = poll.allowMultipleAnswers,
                    options = poll.options,
                    results = poll.options.map { 0 }).toMessage("be")
            )
        )
        PollRepository.updateMessageId(id, message1.messageId)
        reply(message, sentToUsersVoteText(langCode))
    }

fun sentToUsersVoteText(langCode: String) = when (langCode) {
    "be" -> "Ваша апытанне адпраўлена на галасаванне карыстальнікам"
    else -> "Ваш опрос отправлен на голосование пользователям"
}
