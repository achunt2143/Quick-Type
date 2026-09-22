package com.achunt.justtype

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorEvaluatorTest {

    @Test
    fun testBasicArithmetic() {
        val result1 = CalculatorEvaluator.evaluate("45 * 12")
        assertNotNull(result1)
        assertEquals("540", result1?.result)

        val result2 = CalculatorEvaluator.evaluate("100 / 4")
        assertNotNull(result2)
        assertEquals("25", result2?.result)

        val result3 = CalculatorEvaluator.evaluate("15 + 27 - 2")
        assertNotNull(result3)
        assertEquals("40", result3?.result)
    }

    @Test
    fun testHumanFriendlyOperators() {
        val result1 = CalculatorEvaluator.evaluate("12 x 5")
        assertNotNull(result1)
        assertEquals("60", result1?.result)

        val result2 = CalculatorEvaluator.evaluate("100 ÷ 5")
        assertNotNull(result2)
        assertEquals("20", result2?.result)
    }

    @Test
    fun testParenthesesAndOrderOfOperations() {
        val result = CalculatorEvaluator.evaluate("(120 + 30) / 5")
        assertNotNull(result)
        assertEquals("30", result?.result)
    }

    @Test
    fun testPercentageCalculation() {
        val result1 = CalculatorEvaluator.evaluate("15% of 200")
        assertNotNull(result1)
        assertEquals("30", result1?.result)

        val result2 = CalculatorEvaluator.evaluate("20% 50")
        assertNotNull(result2)
        assertEquals("10", result2?.result)
    }

    @Test
    fun testExponents() {
        val result = CalculatorEvaluator.evaluate("2^8")
        assertNotNull(result)
        assertEquals("256", result?.result)
    }

    @Test
    fun testNonMathReturnsNull() {
        assertNull(CalculatorEvaluator.evaluate("hello world"))
        assertNull(CalculatorEvaluator.evaluate("call Alice"))
        assertNull(CalculatorEvaluator.evaluate("1234567"))
        assertNull(CalculatorEvaluator.evaluate(""))
    }

    @Test
    fun testDivisionByZeroHandledGracefully() {
        val result = CalculatorEvaluator.evaluate("10 / 0")
        assertNull(result)
    }
}
