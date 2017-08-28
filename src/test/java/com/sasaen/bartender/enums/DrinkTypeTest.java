package com.sasaen.bartender.enums;

import com.sasaen.bartender.response.DrinkResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by santoss on 28/08/2017.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DrinkTypeTest {

    @Test
    public  void getEffort() throws InterruptedException {
        DrinkType beer = DrinkType.BEER;
        assertEquals(1, beer.getEffort());

        DrinkType drink = DrinkType.DRINK;
        assertEquals(2, drink.getEffort());
    }

    @Test
    public  void getDrinkType() throws InterruptedException {
        assertEquals(DrinkType.BEER, DrinkType.getDrinkType("BEER"));
        assertEquals(DrinkType.DRINK, DrinkType.getDrinkType("DRINK"));

        try {
            DrinkType.getDrinkType(null);
        }catch (IllegalArgumentException e){
            assertEquals("Invalid drink type, valid values: [BEER, DRINK]", e.getMessage());
        }

        try {
            DrinkType.getDrinkType("BEE");
        }catch (IllegalArgumentException e){
         assertEquals("Invalid drink type, valid values: [BEER, DRINK]", e.getMessage());
        }

        try {
            DrinkType.getDrinkType("drink");
        }catch (IllegalArgumentException e){
            assertEquals("Invalid drink type, valid values: [BEER, DRINK]", e.getMessage());
        }

        try {
            DrinkType.getDrinkType("beer");
        }catch (IllegalArgumentException e){
            assertEquals("Invalid drink type, valid values: [BEER, DRINK]", e.getMessage());
        }

    }
}
