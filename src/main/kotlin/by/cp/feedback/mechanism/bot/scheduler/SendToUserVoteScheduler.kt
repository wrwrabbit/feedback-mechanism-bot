package by.cp.feedback.mechanism.bot.scheduler

import by.cp.feedback.mechanism.bot.model.*
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
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
class SendToUserVoteScheduler {

    @Scheduled(fixedRate = 4, timeUnit = TimeUnit.SECONDS)
    fun process() {
        val reviews = PollUserVoteRepository.select15()
        if (reviews.isNotEmpty()) {
            reviews.forEach { review ->
                runBlocking {
                    val poll = PollRepository.getById(review.pollId)!!
                    val langCode = UserRepository.langCodeById(review.userId)
                    bot.execute(
                        SendTextMessage(
                            chatId = review.userId.toChatId(),
                            text = poll.toMessage(langCode),
                            replyMarkup = questionsToMarkup(
                                options = poll.options,
                                pollId = poll.id,
                                allowMultipleAnswers = poll.allowMultipleAnswers
                            )
                        )
                    )
                }
                PollUserVoteRepository.delete(review)
            }
        }
    }

    fun questionsToMarkup(options: Array<String>, pollId: Long, allowMultipleAnswers: Boolean) =
        if (allowMultipleAnswers) {
            InlineKeyboardMarkup(
                matrix {
                    options.mapIndexed { index, it ->
                        row {
                            +CallbackDataInlineKeyboardButton(
                                "❎${index + 1}",
                                callbackData = "${userVoteCheckAnswerDataCallback}${pollId}_${index + 1}"
                            )
                        }
                    }
                    row {
                        +CallbackDataInlineKeyboardButton(
                            "Vote",
                            callbackData = "$userVoteMultipleAnswersDataCallback${pollId}"
                        )
                    }
                }
            )
        } else {
            InlineKeyboardMarkup(
                matrix {
                    options.mapIndexed { index, _ ->
                        row {
                            +CallbackDataInlineKeyboardButton(
                                "${index + 1}",
                                callbackData = "${userVoteDataCallback}${pollId}_${index + 1}"
                            )
                        }
                    }
                }
            )
        }
}
