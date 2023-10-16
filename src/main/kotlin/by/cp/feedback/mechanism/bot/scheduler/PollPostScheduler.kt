package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.behaviour.utils.botLinkMarkup
import by.cp.feedback.mechanism.bot.exceptionLogging
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.bot
import by.cp.feedback.mechanism.bot.model.postChatId
import by.cp.feedback.mechanism.bot.model.toChannelMessage
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
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

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    fun process() {
        runBlocking {
            PollUserVoteRepository.findResults(PollStatus.VOTING).forEach { poll ->
                try {
                    bot.edit(
                        chatId = postChatId.toChatId(),
                        messageId = poll.messageId!!,
                        text = poll.toChannelMessage(),
                        replyMarkup = botLinkMarkup(poll.id)
                    )
                } catch (e: MessageIsNotModifiedException) {
                } catch (e: Exception) {
                    exceptionLogging(e, "Exception while edit post")
                }
            }
        }
    }
}
