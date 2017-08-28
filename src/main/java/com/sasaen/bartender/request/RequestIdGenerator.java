package com.sasaen.bartender.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates sequential ids.
 * <p>
 * Created by santoss on 26/08/2017.
 */
public class RequestIdGenerator {
    public static Logger logger = LoggerFactory.getLogger(RequestIdGenerator.class);

    private static AtomicLong requestIdCounter = new AtomicLong(0);

    public static synchronized long nextId() {
        long andIncrement = requestIdCounter.getAndIncrement();
        logger.info("generated "+andIncrement);
        return andIncrement;
    }

}
