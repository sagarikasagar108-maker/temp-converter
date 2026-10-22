package com.example;

public class Main {

    public static void main(String[] args) {
        // Usage: java -jar temp-converter.jar <c2f|f2c|c2k> <value>
        if (args.length == 2) {
            try {
                double value = Double.parseDouble(args[1]);
                switch (args[0].toLowerCase()) {
                    case "c2f" -> System.out.printf("%.2f C = %.2f F%n", value, TemperatureConverter.celsiusToFahrenheit(value));
                    case "f2c" -> System.out.printf("%.2f F = %.2f C%n", value, TemperatureConverter.fahrenheitToCelsius(value));
                    case "c2k" -> System.out.printf("%.2f C = %.2f K%n", value, TemperatureConverter.celsiusToKelvin(value));
                    default -> {
                        System.out.println("Unknown mode. Use c2f, f2c or c2k.");
                        System.exit(1);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                System.exit(1);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
                System.exit(1);
            }
        } else {
            // No arguments: run a small demo so the container always prints something.
            System.out.println("=== Temperature Converter Demo ===");
            System.out.printf("0 C   = %.2f F%n", TemperatureConverter.celsiusToFahrenheit(0));
            System.out.printf("100 C = %.2f F%n", TemperatureConverter.celsiusToFahrenheit(100));
            System.out.printf("98.6 F = %.2f C%n", TemperatureConverter.fahrenheitToCelsius(98.6));
            System.out.printf("25 C  = %.2f K%n", TemperatureConverter.celsiusToKelvin(25));
            System.out.println("Usage: java -jar temp-converter.jar <c2f|f2c|c2k> <value>");
        }
    }
}
