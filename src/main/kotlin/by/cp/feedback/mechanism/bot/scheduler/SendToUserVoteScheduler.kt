package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.model.userVoteMultipleAnswersMarkup
import by.cp.feedback.mechanism.bot.model.userVoteSingleAnswerMarkup
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
class SendToUserVoteScheduler {

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    fun process() {
        runBlocking {
            PollUserVoteRepository.select15().forEach { polltoVote ->
                val poll = PollRepository.getById(polltoVote.pollId)!!
                bot.execute(
                    SendTextMessage(
                        chatId = polltoVote.userId.toChatId(),
                        text = poll.toMessage(),
                        replyMarkup = if (poll.allowMultipleAnswers) {
                            userVoteMultipleAnswersMarkup(poll.options, poll.id)
                        } else {
                            userVoteSingleAnswerMarkup(poll.options, poll.id)
                        }
                    )
                )
                PollUserVoteRepository.delete(polltoVote)
            }
        }
    }
}
