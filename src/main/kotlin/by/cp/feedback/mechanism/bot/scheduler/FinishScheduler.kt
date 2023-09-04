package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.bot.exceptions.MessageIsNotModifiedException
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit


@Component
class FinishScheduler {

    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    fun process() {
        runBlocking {
            PollRepository.getByStatus(PollStatus.VOTING).forEach { poll ->
                try {
                    val between = Duration.between(poll.startedAt, LocalDateTime.now(ZoneOffset.UTC))
                    if (between.toSeconds() > secondsTillFinish) {
                        PollRepository.finish(poll.id)
                        bot.edit(
                            chatId = postChatId.toChatId(),
                            messageId = poll.messageId!!,
                            text = "ЗАВЕРШЁН\n" + poll.toMessage(),
                            replyMarkup = null
                        )
                    }
                } catch (e: MessageIsNotModifiedException) {
                } catch (e: Exception) {
                    logger.error(e) { "Exception while edit post" }
                }
            }
        }
    }
}
