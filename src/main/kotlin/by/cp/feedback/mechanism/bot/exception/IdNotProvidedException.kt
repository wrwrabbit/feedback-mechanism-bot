package by.cp.feedback.mechanism.bot.exception

class IdNotProvidedException :
    FeedbackBotException("Отправьте номер опроса вместе с командой в одном сообщении, пример: /get_poll 20")