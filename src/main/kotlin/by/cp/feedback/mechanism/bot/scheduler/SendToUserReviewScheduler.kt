package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.sendToUserReviewMarkup
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.MessageQueueRepository
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class SendToUserReviewScheduler {

    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    fun process() {
        runBlocking {
            MessageQueueRepository.select15().forEach { review ->
                bot.execute(
                    SendTextMessage(
                        chatId = review.userId.toChatId(),
                        text = PollRepository.getById(review.pollId)!!.toMessage(),
                        replyMarkup = sendToUserReviewMarkup(review.pollId)
                    )
                )
                MessageQueueRepository.delete(review)
            }
        }
    }
}
