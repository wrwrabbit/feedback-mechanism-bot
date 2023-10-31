package by.cp.feedback.mechanism.bot.exception

class PollNotFoundInDbException(id: String) : FeedbackBotException("Poll with id $id not found in db")