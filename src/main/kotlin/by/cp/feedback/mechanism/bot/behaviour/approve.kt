package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.approvalsRequired
import by.cp.feedback.mechanism.bot.exception.*
import by.cp.feedback.mechanism.bot.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollModerationRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

fun approve(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit =
    { message: CommonMessage<TextContent>, args: Array<String> ->
        try {
            if (message.chat.id.chatId != moderatorsChatId) throw NotModeratorsChatException()
            if (args.size != 1) throw NotOneArgException()
            val id = args.first().toLong()
            val pollModeration = PollModerationRepository.getById(id) ?: throw PollNotFoundInDbException()
            if (pollModeration.rejectionReason != null) throw CantApproveRejectedException()
            val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
            if (userId in pollModeration.approves) {
                throw AlreadyApprovedException()
            }
            val resultArray = pollModeration.approves.plus(userId)
            PollModerationRepository.updateApproves(id, resultArray)
            if (resultArray.size == approvalsRequired) {
                val poll = PollRepository.getById(pollModeration.pollId)!!
                execute(SendTextMessage(poll.userId.toChatId(), "Your poll #${poll.id} approved"))
            }
            reply(message, "You approved this poll, current approves ${resultArray.size}/$approvalsRequired")
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }