package com.example;

/** Pure conversion logic. No input/output here, which makes it easy to test. */
public class TemperatureConverter {

    public static double celsiusToFahrenheit(double c) {
        checkCelsius(c);
        return c * 9.0 / 5.0 + 32.0;
    }

    public static double fahrenheitToCelsius(double f) {
        double c = (f - 32.0) * 5.0 / 9.0;
        checkCelsius(c);
        return c;
    }

    public static double celsiusToKelvin(double c) {
        checkCelsius(c);
        return c + 273.15;
    }

    // Nothing can be colder than absolute zero (-273.15 C).
    private static void checkCelsius(double c) {
        if (c < -273.15) {
            throw new IllegalArgumentException("Temperature is below absolute zero");
        }
    }
}
