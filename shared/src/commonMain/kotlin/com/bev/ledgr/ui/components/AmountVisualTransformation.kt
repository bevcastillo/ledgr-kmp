package com.bev.ledgr.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private const val MAX_WHOLE_DIGITS = 12

fun isValidAmountInput(input: String): Boolean {
    if (input.isEmpty()) return true
    val pattern = Regex("^\\d{0,$MAX_WHOLE_DIGITS}(\\.\\d{0,2})?$")
    return pattern.matches(input)
}

// Shows "1234567.89" as "1,234,567.89" while the field's real value stays "1234567.89"
class AmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val dotIndex = raw.indexOf('.')
        val wholePart = if (dotIndex >= 0) raw.substring(0, dotIndex) else raw
        val decimalPart = if (dotIndex >= 0) raw.substring(dotIndex) else ""

        val groupWhole = wholePart
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        val formatted = groupWhole + decimalPart

        val totalCommas = groupWhole.length - wholePart.length

        val offsetMapping = object : OffsetMapping {
            // Cursor position in the digits
            override fun originalToTransformed(offset: Int): Int {
                if (offset > wholePart.length) return offset + totalCommas
                val commasBefore = (1 until offset).count { index ->
                    (wholePart.length - index) % 3 == 0
                }
                return offset + commasBefore
            }

            // Position in the displayed text -> cursor position in the digits
            override fun transformedToOriginal(offset: Int): Int {
                val safeOffset = offset.coerceIn(0, formatted.length)
                val commasBefore = formatted.take(safeOffset).count { it == ',' }
                return (safeOffset - commasBefore).coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
