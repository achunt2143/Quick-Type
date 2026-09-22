package com.achunt.justtype

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

data class CalculationResult(
    val expression: String,
    val result: String
)

object CalculatorEvaluator {

    private val numberFormatter = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.US))

    /**
     * Evaluates a mathematical expression string. Returns a [CalculationResult] if valid, or null.
     */
    fun evaluate(query: String): CalculationResult? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null

        // Support expressions like "15% of 200"
        val percentOfRegex = Regex("""^(\d+(?:\.\d+)?)\s*%\s*(?:of)?\s*(\d+(?:\.\d+)?)$""", RegexOption.IGNORE_CASE)
        val percentMatch = percentOfRegex.matchEntire(trimmed)
        if (percentMatch != null) {
            val pct = percentMatch.groupValues[1].toDoubleOrNull() ?: return null
            val total = percentMatch.groupValues[2].toDoubleOrNull() ?: return null
            val res = (pct / 100.0) * total
            return CalculationResult(trimmed, numberFormatter.format(res))
        }

        // Replace human operators with standard ones
        var sanitized = trimmed
            .replace("x", "*", ignoreCase = true)
            .replace("×", "*")
            .replace("÷", "/")
            .replace("=", "")
            .trim()

        // Check if string contains at least one operator: +, -, *, /, %, ^
        val hasOperator = sanitized.any { it in "+-*/%^" }
        if (!hasOperator) return null

        // Ensure string only contains allowed characters: digits, operators, parens, dot, spaces
        if (!sanitized.matches(Regex("""^[0-9+\-*/%^().\s]+$"""))) {
            return null
        }

        return try {
            val parser = MathParser(sanitized)
            val value = parser.parse()
            if (value.isFinite()) {
                CalculationResult(trimmed, numberFormatter.format(value))
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private class MathParser(private val str: String) {
        private var pos = -1
        private var ch = 0

        private fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        private fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            while (ch == ' '.code) nextChar()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor | term `%` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | factor `^` factor

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch in '0'.code..'9'.code) || ch == '.'.code) {
                while ((ch in '0'.code..'9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + if (ch != -1) ch.toChar() else "EOF")
            }

            if (eat('^'.code)) x = Math.pow(x, parseFactor())

            return x
        }
    }
}
