package by.cp.feedback.mechanism.bot.behaviour.moderation

import by.cp.feedback.mechanism.bot.behaviour.utils.tryFModerators
import by.cp.feedback.mechanism.bot.model.moderatorsChatId
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun moderatorFixReject(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = tryFModerators { callback ->
    delete((callback as MessageDataCallbackQuery).message)
    execute(SendTextMessage(moderatorsChatId.toChatId(), "Вы отклонили исправленную версию опроса"))
}
