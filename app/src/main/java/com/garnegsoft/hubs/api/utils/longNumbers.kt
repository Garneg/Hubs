package com.garnegsoft.hubs.api.utils

fun formatLongNumbers(number: Int): String {
    if (number > 1_000) {
        if (number <= 10_000) {
            return String.format(
                "%.1f",
                (number.toFloat() / 1_000f)
            ).replace(',', '.') + "K"
        }
        if (number < 1_000_000) {
            return String.format(
                "%.0f",
                (number.toFloat() / 1_000f)
            ).replace(',', '.') + "K"
        }
    }
    if (number > 1_000_000) {
        if (number <= 10_000_000) {
            return String.format(
                "%.1f",
                (number.toFloat() / 1_000_000f)
            ).replace(',', '.') + "M"
        }
        return String.format(
            "%.0f",
            (number.toFloat() / 1_000_000f)
        ).replace(',', '.') + "M"
    }
    return number.toString()

}
