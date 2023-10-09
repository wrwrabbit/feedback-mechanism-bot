package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.model.MessageQueueType.REVIEW
import by.cp.feedback.mechanism.bot.model.MessageQueueType.VOTE
import by.cp.feedback.mechanism.bot.repository.MessageQueueRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class MessageQueueProccessor {

    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    fun process() {
        runBlocking {
            MessageQueueRepository.select15().forEach { entry ->
                try {
                    when (entry.type) {
                        REVIEW -> bot.execute(
                            SendTextMessage(
                                chatId = entry.userId.toChatId(),
                                text = PollRepository.getById(entry.pollId)!!.toMessage("Хотите ли вы, чтобы этот опрос опубликовали?"),
                                replyMarkup = sendToUserReviewMarkup(entry.pollId)
                            )
                        )

                        VOTE -> PollRepository.getById(entry.pollId)!!.let { poll ->
                            bot.execute(
                                SendTextMessage(
                                    chatId = entry.userId.toChatId(),
                                    text = poll.toMessage("Проголосуйте"),
                                    replyMarkup = if (poll.allowMultipleAnswers) {
                                        userVoteMultipleAnswersMarkup(poll.options, poll.id)
                                    } else {
                                        userVoteSingleAnswerMarkup(poll.options, poll.id)
                                    }
                                )
                            )
                        }
                    }
                } catch (ex: Exception) {
                    logger.error(ex) { "Exception while sending message from queue" }
                } finally {
                    MessageQueueRepository.delete(entry)
                }
            }
        }
    }
}
