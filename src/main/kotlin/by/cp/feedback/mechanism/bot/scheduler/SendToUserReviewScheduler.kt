package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.sendToUserReviewMarkup
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.toChatId
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
                    bot.execute(
                        SendTextMessage(
                            chatId = review.userId.toChatId(),
                            text = PollRepository.getById(review.pollId)!!.toMessage(),
                            replyMarkup = sendToUserReviewMarkup(review.pollId)
                        )
                    )
                }
                PollUserReviewRepository.delete(review)
            }
        }
    }
}
