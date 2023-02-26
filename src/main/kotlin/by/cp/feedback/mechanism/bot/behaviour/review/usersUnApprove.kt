package by.cp.feedback.mechanism.bot.behaviour.review

import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userUnApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    delete((callback as MessageDataCallbackQuery).message)
}
