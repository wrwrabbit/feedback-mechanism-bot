package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.secondsTillFinish
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollVoteRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit


@Component
class FinishScheduler {

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    fun process() {
        PollVoteRepository.findInVoting().forEach { poll ->
            val between = Duration.between(poll.startedAt, LocalDateTime.now(ZoneOffset.UTC))
            if (between.toSeconds() > secondsTillFinish) {
                PollRepository.finish(poll.id)
            }
        }
    }
}
