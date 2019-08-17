package it.golovchenko.android.rxkotlin3.utils

import java.util.regex.Pattern

sealed class CardType(val type: Int, val name: String) {
    companion object {
        private val regVisa = Pattern.compile("^4[0-9]{12}(?:[0-9]{3})?$")
        private val regMasterCard = Pattern.compile("^5[1-5][0-9]{14}$")
        private val regAmericanExpress = Pattern.compile("^3[47][0-9]{13}$")

        fun fromNumber(number: String): CardType =
            when {
                regVisa.matcher(number).matches() -> VISA
                regMasterCard.matcher(number).matches() -> MASTER_CARD
                regAmericanExpress.matcher(number).matches() -> AMERICAN_EXPRESS
                else -> UNKNOWN
            }
    }

    object UNKNOWN : CardType(-1, "unknown")
    object VISA : CardType(3, "visa")
    object MASTER_CARD : CardType(3, "master card")
    object AMERICAN_EXPRESS : CardType(4, "american express")
}

fun checkCardChecksum(number: String): Boolean {
    val digits = IntArray(number.length)
    for (i in 0 until number.length) {
        digits[i] = Integer.valueOf(number.substring(i, i + 1))
    }
    return checkCardChecksum(digits)
}

fun checkCardChecksum(digits: IntArray): Boolean {
    var sum = 0
    val length = digits.size
    for (i in 0 until length) {

        // Get digits in reverse order
        var digit = digits[length - i - 1]

        // Every 2nd number multiply with 2
        if (i % 2 == 1) {
            digit *= 2
        }
        sum += if (digit > 9) digit - 9 else digit
    }
//    return sum % 10 == 0

    return digits.isNotEmpty()&& (digits[0] % 2 == 0)
}


fun isValidCvc(cardType: CardType, cvc: String): Boolean = cvc.length == cardType.type




