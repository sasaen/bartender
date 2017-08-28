package com.sasaen.bartender.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * POJO class used in the Rest response.
 * Created by santoss on 26/08/2017.
 */
public class DrinkResponse implements Serializable {


    private String customer;
    private String drinkType;

    @JsonIgnore
    private boolean requestAccepted;

    public DrinkResponse() {

    }

    public String getCustomer() {
        return customer;
    }

    public String getDrinkType() {
        return drinkType;
    }

    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setDrinkType(String drinkType) {
        this.drinkType = drinkType;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }
}
