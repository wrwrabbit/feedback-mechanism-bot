package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.behaviour.utils.botLinkMarkup
import by.cp.feedback.mechanism.bot.behaviour.utils.executeIfNotMuted
import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.MessageQueueRepository
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
class PollUserReviewFinishScheduler {

    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.SECONDS)
    fun process() {
        runBlocking {
            PollRepository.getByStatus(PollStatus.ON_USER_REVIEW).forEach { poll ->
                if (PollUserReviewRepository.approvalsCount(poll.id) >= usersApprovalsRequired) {
                    val message = bot.execute(
                        SendTextMessage(
                            chatId = postChatId.toChatId(),
                            text = PollVoteDto(
                                id = poll.id,
                                userId = poll.userId,
                                question = poll.question,
                                allowMultipleAnswers = poll.allowMultipleAnswers,
                                options = poll.options,
                                results = poll.options.map { 0 }).toMessage(),
                            replyMarkup = botLinkMarkup(poll.id)
                        ),
                    )
                    PollRepository.start(poll.id, message.messageId)
                    MessageQueueRepository.save(poll.id, MessageQueueType.VOTE)
                    executeIfNotMuted(poll.userId, SendTextMessage(poll.userId!!.toChatId(), sentToUsersVoteText()))
                }
            }
        }
    }
}
