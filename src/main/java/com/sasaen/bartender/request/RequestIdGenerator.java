package com.sasaen.bartender.request;

/**
 * Generates sequential ids.
 * <p>
 * Created by santoss on 26/08/2017.
 */
public class RequestIdGenerator {
    private static long requestIdCounter = 0;

    public static synchronized long nextId() {
        return ++requestIdCounter;
    }

}
