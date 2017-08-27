package com.sasaen.bartender.actors;

import com.sasaen.bartender.enums.DrinkStatus;
import com.sasaen.bartender.enums.DrinkType;
import com.sasaen.bartender.result.Result;
import org.springframework.util.Assert;

/**
 * Created by santoss on 25/08/2017.
 */
public class DrinkRequestMessage {

    private final long requestId;
    private final String customer;
    private DrinkType drinkType;

    // DrinkStatus is intentionally not included in hashCode and equals due to objects of this class
    // are meant to be added as keys in Maps and the drinkStatus value is to be updated.
    private DrinkStatus drinkStatus;

    public DrinkRequestMessage(String customer, long requestId, DrinkType drinkType) {
        this.customer = customer;
        this.requestId = requestId;
        this.drinkStatus = com.sasaen.bartender.enums.DrinkStatus.DRINK_REQUESTED;
        this.drinkType = drinkType;

        checkNulls();
    }

    private void checkNulls() {
        Assert.notNull(customer, "customer must not be null");
        Assert.notNull(requestId, "requestId must not be null");
        Assert.notNull(drinkStatus, "drinkStatus must not be null");
        Assert.notNull(drinkType, "drinkType must not be null");
    }

    public DrinkType getDrinkType() {
        return drinkType;
    }

    public long getRequestId() {
        return requestId;
    }

    public String getCustomer() {
        return customer;
    }


    public void setDrinkStatus(DrinkStatus drinkStatus) {
        this.drinkStatus = drinkStatus;
        checkNulls();
    }

    public DrinkStatus getDrinkStatus() {
        return drinkStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrinkRequestMessage that = (DrinkRequestMessage) o;

        if (requestId != that.requestId) return false;
        if (customer != null ? !customer.equals(that.customer) : that.customer != null) return false;
        return drinkType == that.drinkType;
    }

    @Override
    public int hashCode() {
        int result = (int) (requestId ^ (requestId >>> 32));
        result = 31 * result + (customer != null ? customer.hashCode() : 0);
        result = 31 * result + (drinkType != null ? drinkType.hashCode() : 0);
        return result;
    }

    public Result toResult(){
        Result result = new Result();
        result.setDrinkType(getDrinkType().name());
        result.setCustomer(getCustomer());
        return result;
    }

    @Override
    public String toString() {
        return "DrinkRequestMessage{" +
                "requestId='" + requestId + '\'' +
                ", drinkType=" + drinkType +
                ", customer='" + customer + '\'' +
                ", drinkStatus=" + drinkStatus +
                '}';
    }
}
