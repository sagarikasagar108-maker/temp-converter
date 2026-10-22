package com.example;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TemperatureConverterTest {

    private static final double DELTA = 0.01;

    @Test
    void freezingPointCelsiusToFahrenheit() {
        assertEquals(32.0, TemperatureConverter.celsiusToFahrenheit(0), DELTA);
    }

    @Test
    void boilingPointCelsiusToFahrenheit() {
        assertEquals(212.0, TemperatureConverter.celsiusToFahrenheit(100), DELTA);
    }

    @Test
    void negativeFortyIsSameInBothScales() {
        assertEquals(-40.0, TemperatureConverter.celsiusToFahrenheit(-40), DELTA);
        assertEquals(-40.0, TemperatureConverter.fahrenheitToCelsius(-40), DELTA);
    }

    @Test
    void bodyTemperatureFahrenheitToCelsius() {
        assertEquals(37.0, TemperatureConverter.fahrenheitToCelsius(98.6), DELTA);
    }

    @Test
    void celsiusToKelvin() {
        assertEquals(273.15, TemperatureConverter.celsiusToKelvin(0), DELTA);
    }

    @Test
    void belowAbsoluteZeroIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TemperatureConverter.celsiusToFahrenheit(-300));
    }
}
