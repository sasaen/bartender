package com.sasaen.bartender.result;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by santoss on 26/08/2017.
 */
public class Result implements Serializable {


    private String customer;
    private String drinkType;

    @JsonIgnore
    private boolean requestAccepted;

    public Result() {

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
