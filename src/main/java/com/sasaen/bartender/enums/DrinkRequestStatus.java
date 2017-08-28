package com.sasaen.bartender.enums;

/**
 * Enumeration that defines the different status of a drink request.
 *
 * Created by santoss on 26/08/2017.
 */
public enum DrinkRequestStatus {
    DRINK_REQUESTED, DRINK_REQUEST_ACCEPTED, DRINK_REQUEST_ACCEPTED_ACK, DRINK_WORKER_AVAILABLE,
    DRINK_READY, WAIT_TO_BE_SERVED, DRINK_REQUEST_TIMEOUT
}
