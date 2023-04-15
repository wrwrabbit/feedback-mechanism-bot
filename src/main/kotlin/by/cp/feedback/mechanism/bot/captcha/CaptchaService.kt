package by.cp.feedback.mechanism.bot.captcha

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage


object CaptchaService {

    fun getCaptcha(): Pair<BufferedImage, String> = try {
        val backgroundColor: Color = Color.white
        val borderColor: Color = Color.black
        val textColor: Color = Color.black
        val circleColor = Color(190, 160, 150)
        val textFont = Font("Verdana", Font.BOLD, 20)
        val charsToPrint = 6
        val width = 160
        val height = 100
        val circlesToDraw = 25
        val horizMargin = 10.0f
        val rotationRange = 0.7
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = bufferedImage.graphics as Graphics2D
        g.color = backgroundColor
        g.fillRect(0, 0, width, height)
        g.color = circleColor
        for (i in 0 until circlesToDraw) {
            val L = (Math.random() * height / 2.0).toInt()
            val X = (Math.random() * width - L).toInt()
            val Y = (Math.random() * height - L).toInt()
            g.draw3DRect(X, Y, L * 2, L * 2, true)
        }
        g.color = textColor
        g.font = textFont
        val fontMetrics = g.fontMetrics
        val maxAdvance = fontMetrics.maxAdvance
        val fontHeight = fontMetrics.height
        val elegibleChars = "ABCDEFGHJKLMNPQRSTUVWXYabcdefghjkmnpqrstuvwxy23456789"
        val chars = elegibleChars.toCharArray()
        val spaceForLetters = -horizMargin * 2 + width
        val spacePerChar = spaceForLetters / (charsToPrint - 1.0f)
        val finalString = StringBuffer()
        for (i in 0 until charsToPrint) {
            val randomValue = Math.random()
            val randomIndex = Math.round(randomValue * (chars.size - 1)).toInt()
            val characterToShow = chars[randomIndex]
            finalString.append(characterToShow)
            val charWidth = fontMetrics.charWidth(characterToShow)
            val charDim = Math.max(maxAdvance, fontHeight)
            val halfCharDim = (charDim / 2)
            val charImage = BufferedImage(charDim, charDim, BufferedImage.TYPE_INT_ARGB)
            val charGraphics = charImage.createGraphics()
            charGraphics.translate(halfCharDim, halfCharDim)
            val angle = (Math.random() - 0.5) * rotationRange
            charGraphics.transform(AffineTransform.getRotateInstance(angle))
            charGraphics.translate(-halfCharDim, -halfCharDim)
            charGraphics.color = textColor
            charGraphics.font = textFont
            val charX = (0.5 * charDim - 0.5 * charWidth).toInt()
            charGraphics.drawString(
                "" + characterToShow, charX,
                ((charDim - fontMetrics.ascent) / 2 + fontMetrics.ascent)
            )
            val x = horizMargin + spacePerChar * i - charDim / 2.0f
            g.drawImage(charImage, x.toInt(), ((height - charDim) / 2), charDim, charDim, null, null)
            charGraphics.dispose()
        }
        g.color = borderColor
        g.drawRect(0, 0, width - 1, height - 1)
        g.dispose()
        bufferedImage to finalString.toString()
    } catch (ioe: Exception) {
        throw RuntimeException("Unable to build image", ioe)
    }

}