package com.karkia.emojistatusapp.filter

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.Toast

class EmojiFilter : InputFilter {
    private companion object {
        private const val TAG = "EmojiFilter"

        private val VALID_CHAR_TYPES = listOf(
            Character.NON_SPACING_MARK, // 6
            Character.DECIMAL_DIGIT_NUMBER, // 9
            Character.LETTER_NUMBER, // 10
            Character.OTHER_NUMBER, // 11
            Character.SPACE_SEPARATOR, // 12
            Character.FORMAT, // 16
            Character.SURROGATE, // 19
            Character.DASH_PUNCTUATION, // 20
            Character.START_PUNCTUATION, // 21
            Character.END_PUNCTUATION, // 22
            Character.CONNECTOR_PUNCTUATION, // 23
            Character.OTHER_PUNCTUATION, // 24
            Character.MATH_SYMBOL, // 25
            Character.CURRENCY_SYMBOL, //26
            Character.MODIFIER_SYMBOL, // 27
            Character.OTHER_SYMBOL // 28
        ).map { it.toInt() }.toSet()
    }

    private lateinit var mainActivityContext: Context

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        // if input text is valid, return input source
        // if invalid, return empty string
        if (source == null || source.isBlank()) {
            return ""
        }

        Log.i(TAG, "Added text $source, it has a length of ${source.length} characters")

        val validCharTypes = listOf(VALID_CHAR_TYPES)
        for (inputChar in source) {
            val type = Character.getType(inputChar)
            Log.i(TAG, "Input character type: $type")
            if (!validCharTypes.contains(type)) {
                Toast.makeText(
                    mainActivityContext,
                    "only emojis are allowed",
                    Toast.LENGTH_SHORT
                ).show()
                return ""
            }
        }

        Log.i(TAG, "Emoji type is valid")
        return source
    }

    fun setMainActivityContext(mainActivity: Context): EmojiFilter {
        mainActivityContext = mainActivity

        return this
    }
}