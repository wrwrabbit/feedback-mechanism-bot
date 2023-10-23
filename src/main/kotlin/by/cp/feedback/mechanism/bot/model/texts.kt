package by.cp.feedback.mechanism.bot.model

import by.cp.feedback.mechanism.bot.exception.AllowMultipleAnswersNotFoundException
import by.cp.feedback.mechanism.bot.exception.LessThanTwoAnswersException
import by.cp.feedback.mechanism.bot.exception.MoreThanTenAnswersException
import by.cp.feedback.mechanism.bot.exception.QuestionNotFoundException
import by.cp.feedback.mechanism.bot.model.PollStatus.*
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.from
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import io.ktor.util.*

const val changeCaptcha = "Обновить код с картинки"
const val cancelPollCreation = "Отменить создание опроса"

const val myPollsButtonText = "\uD83D\uDDC2 мои опросы"
const val contactAdministrationReplyText = "Отправьте сообщение в ответ на это сообщение"
const val contactAdministrationUserStartText = "Сообщение от создателя опроса "
const val contactAdministrationModerStartText = "Сообщение от администрации:"
const val contactAdministrationMessageSentText = "Вы отправили сообщение"
const val contactAdministrationButtonText = "\uD83E\uDE9B связь с администрацией"

fun CommonMessage<*>.langCode() =
    (this.from!! as CommonUser).ietfLanguageCode?.code ?: "ru"

fun question() = "Вопрос"

fun answer() = "Ответ"

fun sentToUsersVoteText() = "Ваш опрос отправлен на голосование пользователям"

fun sendMeCaptchaText() = "Чтобы убедиться, что вы не робот, введите код с картинки"

fun wrongCaptchaText() = "Неправильная капча"

fun voteResultText(messageId: Long?) = "Вы проголосовали в опросе" +
        messageId?.let {
            "\nВы можете получить результаты опроса по " +
                    "[ссылке](https://t.me/c/${postChatId.toString().substring(4)}/$messageId)"
        }

fun moreThanOneAnswer() = "Больше одного ответа"

fun writeStart() = "Вы не зарегистрированы, пожалуйста, напишите команду /start"

fun sentToModeratorsText(pollDto: PollDto) = pollDto.toMessage("Ваш опрос отправлен модераторам")

fun PollVoteDto.toChannelMessage(): String = "Опрос #$id\n" +
        "${question()}: $question\n" +
        this.results() +
        "${moreThanOneAnswer()}: ${allowMultipleAnswers.toAllowMultipleAnswersChannel()}\n"

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

fun PollDto.toMessage(messageToUser: String): String = "Опрос #$id\n" +
        "Статус: ${status.toMessage()}\n" +
        "=====\n" +
        "$messageToUser\n" +
        "=====\n" +
        "${question()}: $question\n" +
        "${options.mapIndexed { i, option -> "${answer()}: ${i + 1}: $option" }.joinToString("\n")}\n" +
        "${moreThanOneAnswer()}: ${allowMultipleAnswers.toAllowMultipleAnswers()}"

fun PollStatus.toMessage(): String = when (this) {
    ON_MODERATOR_REVIEW -> "На рассмотрении модераторов"
    REJECTED -> "Отклонён"
    ON_USER_REVIEW -> "На рассмотрении пользователей"
    VOTING -> "Голосование"
    FINISHED -> "Завершён"
}

fun PollDto.toModeratorsMessage(): String = "Опрос #$id\n" +
        "Статус ${status.toMessage()}\n" +
        "${question()}: $question\n" +
        "${options.joinToString("\n") { option -> "${answer()}: $option" }}\n" +
        "${moreThanOneAnswer()}: ${allowMultipleAnswers.toAllowMultipleAnswers()}"

fun Boolean.toAllowMultipleAnswers(): String = if (this) {
    "Да"
} else {
    "Нет"
}

fun Boolean.toAllowMultipleAnswersChannel(): String = if (this) {
    "Да (сумма > 100%)"
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
    if (options.size > 10) {
        throw MoreThanTenAnswersException()
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
