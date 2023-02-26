package by.cp.feedback.mechanism.bot.behaviour.common

import by.cp.feedback.mechanism.bot.model.languageDataCallback
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun chooseLanguage(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val langCode = callback.data.substring(languageDataCallback.length)
    val userId: Long = callback.user.id.chatId
    UserRepository.updateLangCode(userId, langCode)
    delete((callback as MessageDataCallbackQuery).message)
}
