package by.cp.feedback.mechanism.bot.model

import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.SimpleKeyboardButton
import dev.inmo.tgbotapi.utils.matrix
import dev.inmo.tgbotapi.utils.row

fun menuMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +SimpleKeyboardButton("✍️ создать опрос")
        }
        row {
            +SimpleKeyboardButton("\uD83D\uDDC2 мои опросы")
        }
    },
    resizeKeyboard = true
)

fun endMarkup() = ReplyKeyboardMarkup(
    keyboard = matrix {
        row {
            +SimpleKeyboardButton("Завершить")
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
        }
    },
    oneTimeKeyboard = true,
    resizeKeyboard = true
)

fun moderatorsReviewMarkup(pollId: Long) = InlineKeyboardMarkup(
    matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅ 0/$moderatorsApprovalsRequired",
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



fun userModerationReviewMarkup(pollId: Long) = InlineKeyboardMarkup(
    matrix {
        row {
            +CallbackDataInlineKeyboardButton(
                "✅",
                callbackData = "$userApproveModerationDC$pollId"
            )
            +CallbackDataInlineKeyboardButton(
                "❎",
                callbackData = "$userRejectModerationDC$pollId"
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
                "Vote",
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

fun moderatorsApproveMarkup(pollId: Long, approvalsCount: Int) = InlineKeyboardMarkup(matrix {
    row {
        +CallbackDataInlineKeyboardButton(
            "✅ $approvalsCount/$moderatorsApprovalsRequired",
            callbackData = "$moderatorApproveDC$pollId"
        )
    }
})
