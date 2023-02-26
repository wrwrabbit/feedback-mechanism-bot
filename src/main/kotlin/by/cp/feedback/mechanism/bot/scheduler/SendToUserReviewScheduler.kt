package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.model.userApproveDataCallback
import by.cp.feedback.mechanism.bot.model.userUnApproveDataCallback
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.toChatId
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SendToUserReviewScheduler {

    @Scheduled(fixedRate = 4, timeUnit = TimeUnit.SECONDS)
    fun process() {
        val reviews = PollUserReviewRepository.select15()
        if (reviews.isNotEmpty()) {
            reviews.forEach { review ->
                runBlocking {
                    val langCode = UserRepository.langCodeById(review.userId)
                    bot.execute(SendTextMessage(
                        chatId = review.userId.toChatId(),
                        text = PollRepository.getById(review.pollId)!!.toMessage(langCode),
                        replyMarkup = InlineKeyboardMarkup(
                            matrix {
                                row {
                                    +CallbackDataInlineKeyboardButton(
                                        "Approve",
                                        callbackData = "$userApproveDataCallback${review.pollId}"
                                    )
                                    +CallbackDataInlineKeyboardButton(
                                        "UnApprove",
                                        callbackData = "$userUnApproveDataCallback${review.pollId}"
                                    )
                                }
                            }
                        )))
                }
                PollUserReviewRepository.delete(review)
            }
        }
    }
}
