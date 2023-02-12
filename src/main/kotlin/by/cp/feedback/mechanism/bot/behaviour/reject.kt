package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.exception.*
import by.cp.feedback.mechanism.bot.moderatorsChatId
import by.cp.feedback.mechanism.bot.repository.PollModerationRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId

public fun reject(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit =
    { message: CommonMessage<TextContent>, args: Array<String> ->
        try {
            if (message.chat.id.chatId != moderatorsChatId) throw NotModeratorsChatException()
            if (args.size != 2) throw NotTwoArgException()
            val id = args.first().toLong()
            val rejectionReason = args[1]
            val pollModeration = PollModerationRepository.getById(id) ?: throw PollNotFoundInDbException()
            if (pollModeration.rejectionReason != null) throw CantRejectRejectedException()
            PollModerationRepository.updateRejectionReason(id, rejectionReason)
            val poll = PollRepository.getById(pollModeration.pollId)!!
            execute(SendTextMessage(poll.userId.toChatId(), "Your poll #${poll.id} rejected"))
            reply(message, "You rejected poll #${pollModeration.pollId}")
        } catch (exception: FeedbackBotException) {
            reply(message, exception.message)
        }
    }