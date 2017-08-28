package com.sasaen.bartender.actors;

import com.sasaen.bartender.enums.DrinkRequestStatus;
import com.sasaen.bartender.enums.DrinkType;
import com.sasaen.bartender.response.DrinkResponse;
import org.springframework.util.Assert;

/**
 * Created by santoss on 25/08/2017.
 */
public class DrinkRequestMessage {

    /**
     * The request id is unique.
     * This is the only field used in equals() and hashCode() for simplicity/performance.
     */
    private final long requestId;

    /**
     * The customer name.
     */
    private final String customer;

    /**
     * The drink type.
     */
    private final DrinkType drinkType;

    /**
     * The drink type, subject to be updated by actors.
     */
    private DrinkRequestStatus drinkStatus;

    public DrinkRequestMessage(String customer, long requestId, DrinkType drinkType) {
        this.customer = customer;
        this.requestId = requestId;
        this.drinkStatus = DrinkRequestStatus.DRINK_REQUESTED;
        this.drinkType = drinkType;

        checkNulls();
    }

    /**
     * Creates a <code>DrinkResponse</code> object from this message.
     * @return
     */
    public DrinkResponse toResult() {
        DrinkResponse result = new DrinkResponse();
        result.setDrinkType(getDrinkType().name());
        result.setCustomer(getCustomer());
        return result;
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

    public void setDrinkStatus(DrinkRequestStatus drinkStatus) {
        this.drinkStatus = drinkStatus;
        checkNulls();
    }

    public DrinkRequestStatus getDrinkStatus() {
        return drinkStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DrinkRequestMessage that = (DrinkRequestMessage) o;

        return requestId == that.requestId;
    }

    @Override
    public int hashCode() {
        return (int) (requestId ^ (requestId >>> 32));
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

    private void checkNulls() {
        Assert.notNull(customer, "customer must not be null");
        Assert.notNull(requestId, "requestId must not be null");
        Assert.notNull(drinkStatus, "drinkStatus must not be null");
        Assert.notNull(drinkType, "drinkType must not be null");
    }
}
