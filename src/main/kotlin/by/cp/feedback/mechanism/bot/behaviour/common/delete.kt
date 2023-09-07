package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.behaviour.utils.tryF
import by.cp.feedback.mechanism.bot.exception.FromNotFoundException
import by.cp.feedback.mechanism.bot.model.UserStatus
import by.cp.feedback.mechanism.bot.repository.UserDeletionRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.random.Random

val userDeletionsMap = mutableMapOf<Long, Int>()

fun delete(): suspend BehaviourContext.(CommonMessage<TextContent>) -> Unit = tryF { message ->
    val userId: Long = message.from?.id?.chatId ?: throw FromNotFoundException()
    val count = UserDeletionRepository.countDayAgo(userId)
    if (count <= getOrRandomAndPut(userId)) {
        UserRepository.deleteDataByUserId(userId)
        UserDeletionRepository.save(userId)
        reply(message, "Все ваши данные удалены")
    } else {
        reply(message, "Лимит удалений исчерпан")
    }
}

fun getOrRandomAndPut(userId: Long): Int = userDeletionsMap[userId]
    ?: Random.nextInt(3, 5).let { randomized ->
        userDeletionsMap[userId] = randomized
        randomized
    }

@Component
class UserDeletionsCleaner {

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    fun clean() = userDeletionsMap.clear()
}
