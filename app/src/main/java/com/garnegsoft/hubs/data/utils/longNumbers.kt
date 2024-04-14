package com.garnegsoft.hubs.data.utils

fun formatLongNumbers(number: Int): String {
    if (number > 1_000) {
        if (number <= 10_000) {
            return String.format(
                "%.1f",
                (number.toFloat() / 1_000f)
            ).replace(',', '.').replace(".0", "") + "K"
        }
        if (number < 1_000_000) {
            return String.format(
                "%.0f",
                (number.toFloat() / 1_000f)
            ).replace(',', '.').replace(".0", "") + "K"
        }
    }
    if (number > 1_000_000) {
        if (number <= 10_000_000) {
            return String.format(
                "%.1f",
                (number.toFloat() / 1_000_000f)
            ).replace(',', '.').replace(".0", "") + "M"
        }
        return String.format(
            "%.0f",
            (number.toFloat() / 1_000_000f)
        ).replace(',', '.').replace(".0", "") + "M"
    }
    return number.toString()

}
