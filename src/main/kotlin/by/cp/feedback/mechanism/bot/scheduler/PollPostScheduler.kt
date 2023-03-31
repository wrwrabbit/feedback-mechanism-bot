package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.postChatId
import by.cp.feedback.mechanism.bot.model.toMessage
import by.cp.feedback.mechanism.bot.repository.PollVoteRepository
import dev.inmo.tgbotapi.bot.exceptions.MessageIsNotModifiedException
import dev.inmo.tgbotapi.extensions.api.edit.edit
import dev.inmo.tgbotapi.types.toChatId
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
class PollPostScheduler {

    private val logger = KotlinLogging.logger {}

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.SECONDS)
    fun process() {
        PollVoteRepository.findInVoting().forEach { poll ->
            runBlocking {
                try {
                    bot.edit(
                        chatId = postChatId.toChatId(),
                        messageId = poll.messageId!!,
                        text = poll.toMessage("ru")
                    )
                } catch (e: MessageIsNotModifiedException) {
                } catch (e: Exception) {
                    logger.error(e) { "Exception while edit post" }
                }
            }
        }
    }
}
