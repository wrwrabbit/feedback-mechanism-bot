package by.cp.feedback.mechanism.bot.model

import dev.inmo.tgbotapi.types.buttons.*
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row

fun menuMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +RequestPollKeyboardButton("✍️ создать опрос", requestPoll = RegularKeyboardButtonPollType)
        }
        row {
            +SimpleKeyboardButton(contactAdministrationButtonText)
        }
        row {
            +SimpleKeyboardButton(myPollsButtonText)
        }
    },
    resizeKeyboard = true
)

fun endMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +SimpleKeyboardButton("Завершить")
            +SimpleKeyboardButton(cancelPollCreation)
        }
    },
    oneTimeKeyboard = true,
    resizeKeyboard = true
)

fun yesNoMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +SimpleKeyboardButton("Да")
            +SimpleKeyboardButton("Нет")
            +SimpleKeyboardButton(cancelPollCreation)
        }
    },
    oneTimeKeyboard = true,
    resizeKeyboard = true
)

fun moderatorsReviewMarkup(pollId: Long, approvalsCount: Int) = InlineKeyboardMarkup(
    matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅ $approvalsCount/$moderatorsApprovalsRequired",
                callbackData = "$moderatorApproveDC$pollId"
            )
            +CallbackDataInlineKeyboardButton(
                "✍️",
                callbackData = "$moderatorFixDC$pollId"
            )
            +CallbackDataInlineKeyboardButton(
                "❎",
                callbackData = "$moderatorRejectDC$pollId"
            )
        }
    }
)

fun sendToUserReviewMarkup(pollId: Long) = InlineKeyboardMarkup(
    matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅",
                callbackData = "$userApproveDC$pollId"
            )
            +CallbackDataInlineKeyboardButton(
                "❎",
                callbackData = "$userUnApproveDC$pollId"
            )
        }
    }
)


fun userModerationReviewMarkup(pollId: Long, fixedPoll: String) = InlineKeyboardMarkup(
    matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅",
                callbackData = "${userApproveModerationDC}${pollId}"
            )
            +CallbackDataInlineKeyboardButton(
                "❎",
                callbackData = "$userRejectModerationDC$pollId"
            )
        }
    }
)

fun moderatorsFixMarkup(originalMessageId: Long, pollId: Long) = InlineKeyboardMarkup(
    matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅",
                callbackData = "$moderatorFixApproveDC${originalMessageId}_${pollId}"
            )
            +CallbackDataInlineKeyboardButton(
                "❎",
                callbackData = "$moderatorFixRejectDC${originalMessageId}_${pollId}"
            )
        }
    }
)

fun userVoteMultipleAnswersMarkup(options: Array<String>, pollId: Long) = InlineKeyboardMarkup(
    matrix {
        options.mapIndexed { index, it ->
            row {
                +CallbackDataInlineKeyboardButton(
                    "❎${index + 1}",
                    callbackData = "${userVoteCheckAnswerDC}${pollId}_${index + 1}"
                )
            }
        }
        row {
            +CallbackDataInlineKeyboardButton(
                "Отправить",
                callbackData = "$userVoteMultipleAnswersDC${pollId}"
            )
        }
    }
)

fun userVoteSingleAnswerMarkup(options: Array<String>, pollId: Long) = InlineKeyboardMarkup(
    matrix {
        options.mapIndexed { index, _ ->
            row {
                +CallbackDataInlineKeyboardButton(
                    "${index + 1}",
                    callbackData = "${userVoteDC}${pollId}_${index + 1}"
                )
            }
        }
    }
)

fun showModerationMarkup(pollId: Long) = InlineKeyboardMarkup(
    keyboard = matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                text = "Показать",
                callbackData = "${showModerationDC}${pollId}"
            )
        }
    }
)

fun changeCaptchaMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +SimpleKeyboardButton(changeCaptcha)
        }
    },
    oneTimeKeyboard = true,
    resizeKeyboard = true
)

fun cancelPollCreationMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +SimpleKeyboardButton(cancelPollCreation)
        }
    },
    oneTimeKeyboard = true,
    resizeKeyboard = true
)
