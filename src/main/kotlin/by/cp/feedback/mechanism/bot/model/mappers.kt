package by.cp.feedback.mechanism.bot.model

import by.cp.feedback.mechanism.bot.exception.AllowMultipleAnswersNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessThanTwoAnswersException
import by.cp.feedback.mechanism.bot.exception.QuestionNotFoundException
import io.ktor.util.*

fun Boolean.toAllowMultipleAnswers(): String =
    if (this) {
        "Да"
    } else {
        "Нет"
    }

fun PollDto.toMessage(): String =
    "Опрос #$id\n" +
        "Статус #$status\n" +
        "Вопрос: $question\n" +
        "${options.map { option -> "Ответ: $option\n" }}\n" +
        "Больше одного ответа: ${allowMultipleAnswers.toAllowMultipleAnswers()}"

fun PollDto.toStatusMessage(): String =
    "Опрос #$id," +
        "Статус #$status" +
        if (status == PollStatus.REJECTED) ",Причина отказа: $rejectionReason" else ""

fun parsePoll(text: String): Triple<String, Array<String>, Boolean> {
    val lines = text.split("\n")
    val question = lines.find { line -> line.startsWith("Вопрос: ") }?.substring(8)
        ?: throw QuestionNotFoundException()
    val options = lines.filter { line -> line.startsWith("Ответ: ") }
        .map { line -> line.substring(7) }
        .toTypedArray()
    if (options.size < 2) {
        throw LessThanTwoAnswersException()
    }
    val allowMultipleAnswers = lines.find { line -> line.startsWith("Больше одного ответа: ") }?.substring(22)
        ?.toLowerCasePreservingASCIIRules().let { string ->
            when (string) {
                "да" -> true
                "нет" -> false
                else -> null
            }
        } ?: throw AllowMultipleAnswersNotFoundException()
    return Triple(question, options, allowMultipleAnswers)
}
