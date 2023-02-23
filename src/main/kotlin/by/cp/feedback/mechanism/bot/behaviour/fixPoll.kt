package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.*
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row

fun fixPoll(): suspend BehaviourContext.(CommonMessage<TextContent>, Array<String>) -> Unit = tryF { message, args ->
    if (args.size != 1) throw NotOneArgException()
    val id = args.first().toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    if (userId != poll.userId) throw YouAreNotOwnerOfPollException()
    if (poll.rejectionReason == null) throw PollNotRejectedException()
    val text = message.replyTo?.text ?: throw TextNotFoundInReplyException()
    val langCode = message.langCode()
    val (question, options, allowMultipleAnswers) = parsePoll(text, langCode)
    val updatedPoll = PollRepository.updatePoll(id, question, options, allowMultipleAnswers)
    val markup = InlineKeyboardMarkup(
        matrix {
            row {
                +CallbackDataInlineKeyboardButton("✅", callbackData = "Approve $id")
            }
        }
    )
    execute(SendTextMessage(moderatorsChatId.toChatId(), updatedPoll.toMessage(langCode), replyMarkup = markup))
    reply(message, sentToModeratorsText(langCode))
}
