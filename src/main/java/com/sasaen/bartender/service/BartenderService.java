package com.sasaen.bartender.service;

import com.sasaen.bartender.response.DrinkResponse;

import java.util.List;

/**
 * This interface defines the contract of the bartender service.
 *
 * <p>
 * Created by santoss on 28/08/2017.
 */
public interface BartenderService {


    /**
     *
     * @return the List of served drinks.
     */
    List<DrinkResponse> getServedDrinks();

    /**
     * Requests a drink to a DrinkRequestActor and waits a number of milliseconds for the response.
     * If a DRINK_REQUEST_ACCEPTED is received then a DRINK_REQUEST_ACCEPTED_ACK message is sent.
     * <p>
     * If there is a timeout or the response is not DRINK_REQUEST_ACCEPTED then it sends a DRINK_REQUEST_TIMEOUT
     * to free resources.
     *
     * @param customer  - The customer name.
     * @param drinkType - The drink type string. Possible valid values: BEER and DRINK, case sensitive.
     * @return the response to the request.
     */
    DrinkResponse requestDrink(String customer, String drinkType);
}
