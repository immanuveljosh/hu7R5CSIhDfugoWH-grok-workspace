package com.pulse.dialer

import com.pulse.dialer.util.PhoneNumbers
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumbersTest {
    @Test
    fun keepsDialableSymbols() {
        assertEquals("+919840011223", PhoneNumbers.digitsAndSymbols("+91 (984) 001-1223"))
        assertEquals("1*23#", PhoneNumbers.digitsAndSymbols("1*23#"))
    }

    @Test
    fun formatsCommonNumbers() {
        assertEquals("+91 42233 55667", PhoneNumbers.display("+914223355667"))
        assertEquals("+1 415 555 0192", PhoneNumbers.display("+14155550192"))
        assertEquals("984 001 1223", PhoneNumbers.display("9840011223"))
    }

    @Test
    fun initialsFromName() {
        assertEquals("PN", PhoneNumbers.initials("Priya Natarajan"))
        assertEquals("AM", PhoneNumbers.initials("Amara"))
        assertEquals("#", PhoneNumbers.initials("  "))
    }
}
