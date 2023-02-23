package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.exception.NotOneArgException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.exception.YouAreNotOwnerOfPollException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.langCode
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent

fun sendToUsersReview(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit =
    tryF { message, args ->
        if (args.size != 1) throw NotOneArgException()
        val id = args.first().toLong()
        val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
        val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
        if (userId != poll.userId) throw YouAreNotOwnerOfPollException()
        PollRepository.updateStatus(poll.id, PollStatus.ON_USER_REVIEW)
        PollUserReviewRepository.save(poll.id)
        reply(message, sentToUsersReviewText(message.langCode()))
    }

fun sentToUsersReviewText(langCode: String) = when (langCode) {
    "be" -> "Ваша апытанне адпраўлена на перагляд карыстальнікам"
    else -> "Ваш опрос отправлен на пересмотр пользователям"
}
