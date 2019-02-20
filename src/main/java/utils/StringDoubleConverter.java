package utils;

import javafx.util.StringConverter;

public class StringDoubleConverter extends StringConverter<Double> {
    @Override
    public String toString(Double aDouble) {
        if (aDouble == null) {
            return null;
        }
        return Double.toString(aDouble);
    }

    @Override
    public Double fromString(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
