package com.sasaen.bartender.enums;

import java.util.Arrays;

/**
 * Created by santoss on 26/08/2017.
 */
public enum DrinkType {
    BEER(1), DRINK(2);

    private int effort;

    DrinkType(int effort) {
        this.effort = effort;
    }

    public int getEffort() {
        return effort;
    }

    public static DrinkType getDrinkType(String drinkTypeString) {
        try {
            return DrinkType.valueOf(DrinkType.class, drinkTypeString);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid drink type, valid values: " + Arrays.asList(DrinkType.values()));
        }
    }
}
