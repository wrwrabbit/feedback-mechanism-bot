package by.cp.feedback.mechanism.bot.model

import by.cp.feedback.mechanism.bot.exception.AllowMultipleAnswersNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessThanTwoAnswersException
import by.cp.feedback.mechanism.bot.exception.QuestionNotFoundException
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import io.ktor.util.*

const val changeCaptcha = "Изменить капчу"
const val cancelPollCreation = "Отменить создание опроса"

fun CommonMessage<*>.langCode() =
    (this.from!! as CommonUser).ietfLanguageCode?.code ?: "ru"

fun question() = "Вопрос"

fun answer() = "Ответ"

fun moreThanOneAnswer() = "Больше одного ответа"

fun sentToModeratorsText() = "Ваш опрос отправлен модераторам"

fun PollVoteDto.toMessage(): String = "Опрос #$id\n" +
        "${question()}: $question\n" +
        this.results() +
        "${moreThanOneAnswer()}: ${allowMultipleAnswers.toAllowMultipleAnswers()}\n"

fun PollVoteDto.results(): String = results.reduce { acc, next -> acc + next }
    .let { allAnswers ->
        options.mapIndexed { index, option ->
            val answersCount = results[index]
            val percents = if (allAnswers == 0L) {
                0f
            } else {
                (answersCount.toFloat() / allAnswers.toFloat()) * 100f
            }
            "- $answersCount/$allAnswers ${String.format("%.2f", percents)}% - $option"
        }.joinToString("\n") + "\n"
    }

fun PollDto.toMessage(): String = "Опрос #$id\n" +
        "Статус #$status\n" +
        "${question()}: $question\n" +
        "${options.mapIndexed { i, option -> "${answer()}: ${i + 1}: $option" }.joinToString("\n")}\n" +
        "${moreThanOneAnswer()}: ${allowMultipleAnswers.toAllowMultipleAnswers()}"

fun PollDto.toModeratorsMessage(): String = "Опрос #$id\n" +
        "Статус #$status\n" +
        "${question()}: $question\n" +
        "${options.map { option -> "${answer()}: $option" }.joinToString("\n")}\n" +
        "${moreThanOneAnswer()}: ${allowMultipleAnswers.toAllowMultipleAnswers()}"

fun Boolean.toAllowMultipleAnswers(): String = if (this) {
    "Да"
} else {
    "Нет"
}

fun String.fromAllowMultipleAnswers(): Boolean = when (this) {
    "да" -> true
    "нет" -> false
    else -> null
} ?: throw AllowMultipleAnswersNotFoundException()

fun parsePoll(text: String): Triple<String, Array<String>, Boolean> {
    val lines = text.split("\n")
    val questionS = "${question()}: "
    val question = lines.find { line -> line.startsWith(questionS) }?.substring(questionS.length)
        ?: throw QuestionNotFoundException()
    val answerS = "${answer()}: "
    val options = lines.filter { line -> line.startsWith(answerS) }
        .map { line -> line.substring(answerS.length) }
        .toTypedArray()
    if (options.size < 2) {
        throw LessThanTwoAnswersException()
    }
    val moreThanOneAnswerS = "${moreThanOneAnswer()}: "
    val allowMultipleAnswers =
        lines.find { line -> line.startsWith(moreThanOneAnswerS) }?.substring(moreThanOneAnswerS.length)
            ?.toLowerCasePreservingASCIIRules()
            .let { string ->
                string?.fromAllowMultipleAnswers() ?: throw AllowMultipleAnswersNotFoundException()
            }
    return Triple(question, options, allowMultipleAnswers)
}
