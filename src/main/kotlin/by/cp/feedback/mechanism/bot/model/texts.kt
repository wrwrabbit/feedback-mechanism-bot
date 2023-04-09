package by.cp.feedback.mechanism.bot.model

import by.cp.feedback.mechanism.bot.exception.AllowMultipleAnswersNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessThanTwoAnswersException
import by.cp.feedback.mechanism.bot.exception.QuestionNotFoundException
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import io.ktor.util.*

const val changeCaptcha = "Изменить капчу"

fun CommonMessage<*>.langCode() =
    (this.from!! as CommonUser).ietfLanguageCode?.code ?: "ru"

fun question(langCode: String) = when (langCode) {
    "be" -> "Пытанне"
    else -> "Вопрос"
}

fun answer(langCode: String) = when (langCode) {
    "be" -> "Адказ"
    else -> "Ответ"
}

fun moreThanOneAnswer(langCode: String) = when (langCode) {
    "be" -> "Больш аднаго адказа"
    else -> "Больше одного ответа"
}

fun sentToModeratorsText(langCode: String) = when (langCode) {
    "be" -> "Ваша апытанне адпраўлена мадэратарам"
    else -> "Ваш опрос отправлен модераторам"
}

fun PollVoteDto.toMessage(langCode: String): String = when (langCode) {
    "be" -> "Апытанне #$id\n" +
            "${question(langCode)}: $question\n" +
            this.results() +
            "${moreThanOneAnswer(langCode)}: ${allowMultipleAnswers.toAllowMultipleAnswers(langCode)}\n"

    else -> "Опрос #$id\n" +
            "${question(langCode)}: $question\n" +
            this.results() +
            "${moreThanOneAnswer(langCode)}: ${allowMultipleAnswers.toAllowMultipleAnswers(langCode)}\n"
}

fun PollVoteDto.results(): String = results.reduce { acc, next -> acc + next }
    .let { allAnswers ->
        options.mapIndexed { index, option ->
            val answersCount = results[index]
            val percents = if (allAnswers == 0L) {
                0
            } else {
                answersCount / allAnswers * 100
            }
            "- $answersCount/$allAnswers $percents% - $option"
        }.joinToString("\n") + "\n"
    }

fun PollDto.toMessage(langCode: String): String = when (langCode) {
    "be" -> "Апытанне #$id\n" +
            "Статус #$status\n" +
            "${question(langCode)}: $question\n" +
            "${options.mapIndexed { i, option -> "${answer(langCode)}: ${i + 1}: $option" }.joinToString("\n")}\n" +
            "${moreThanOneAnswer(langCode)}: ${allowMultipleAnswers.toAllowMultipleAnswers(langCode)}"

    else -> "Опрос #$id\n" +
            "Статус #$status\n" +
            "${question(langCode)}: $question\n" +
            "${options.mapIndexed { i, option -> "${answer(langCode)}: ${i + 1}: $option" }.joinToString("\n")}\n" +
            "${moreThanOneAnswer(langCode)}: ${allowMultipleAnswers.toAllowMultipleAnswers(langCode)}"
}

fun Boolean.toAllowMultipleAnswers(langCode: String): String = when (langCode) {
    "be" -> if (this) {
        "Так"
    } else {
        "Не"
    }

    else -> if (this) {
        "Да"
    } else {
        "Нет"
    }
}

fun String.fromAllowMultipleAnswers(langCode: String): Boolean = when (langCode) {
    "be" -> when (this) {
        "так" -> true
        "не" -> false
        else -> null
    } ?: throw AllowMultipleAnswersNotFoundException()

    else -> when (this) {
        "да" -> true
        "нет" -> false
        else -> null
    } ?: throw AllowMultipleAnswersNotFoundException()
}

fun parsePoll(text: String, langCode: String): Triple<String, Array<String>, Boolean> {
    val lines = text.split("\n")
    val questionS = "${question(langCode)}: "
    val question = lines.find { line -> line.startsWith(questionS) }?.substring(questionS.length)
        ?: throw QuestionNotFoundException()
    val answerS = "${answer(langCode)}: "
    val options = lines.filter { line -> line.startsWith(answerS) }
        .map { line -> line.substring(answerS.length) }
        .toTypedArray()
    if (options.size < 2) {
        throw LessThanTwoAnswersException()
    }
    val moreThanOneAnswerS = "${moreThanOneAnswer(langCode)}: "
    val allowMultipleAnswers =
        lines.find { line -> line.startsWith(moreThanOneAnswerS) }?.substring(moreThanOneAnswerS.length)
            ?.toLowerCasePreservingASCIIRules()
            .let { string ->
                string?.fromAllowMultipleAnswers(langCode) ?: throw AllowMultipleAnswersNotFoundException()
            }
    return Triple(question, options, allowMultipleAnswers)
}
