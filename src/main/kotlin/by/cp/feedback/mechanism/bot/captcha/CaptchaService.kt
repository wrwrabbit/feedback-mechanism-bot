package by.cp.feedback.mechanism.bot.captcha

object CaptchaService {

    private fun randomStringByKotlinRandom(length: Int) =
        List(length) { (('a'..'z') + ('A'..'Z') + ('0'..'9')).random() }.joinToString("")

    fun getCaptcha() = randomStringByKotlinRandom(4)

}