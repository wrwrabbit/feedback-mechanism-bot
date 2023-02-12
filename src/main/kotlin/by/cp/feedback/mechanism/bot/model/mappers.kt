package by.cp.feedback.mechanism.bot.model

fun Boolean.toAllowMultipleAnswers(): String =
    if (this) {
        "Да"
    } else {
        "Нет"
    }

fun PollDto.toMessage(): String =
    "Опрос #$id\nВопрос: $question\n${options.map { option -> "Ответ: $option\n" }}\n" +
        "Больше одного ответа: ${allowMultipleAnswers.toAllowMultipleAnswers()}"