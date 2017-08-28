package com.sasaen.bartender.response;

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
public class DrinkResponseTest {

    @Test
    public  void testSetterGetterMethods() throws InterruptedException {
        DrinkResponse response = new DrinkResponse();

        assertNull(response.getCustomer());
        assertNull(response.getDrinkType());
        assertFalse(response.isRequestAccepted());

        response.setRequestAccepted(true);

        assertNull(response.getCustomer());
        assertNull(response.getDrinkType());
        assertTrue(response.isRequestAccepted());

        response.setCustomer("A");
        assertEquals("A", response.getCustomer());
        assertNull(response.getDrinkType());
        assertTrue(response.isRequestAccepted());

        response.setDrinkType("BEER");
        assertEquals("A", response.getCustomer());
        assertEquals("BEER", response.getDrinkType());
        assertTrue(response.isRequestAccepted());
    }
}
