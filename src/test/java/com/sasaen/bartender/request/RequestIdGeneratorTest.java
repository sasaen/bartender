package com.sasaen.bartender.request;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import static org.junit.Assert.*;

/**
 * Created by santoss on 28/08/2017.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class RequestIdGeneratorTest {

    public static Logger logger = LoggerFactory.getLogger(RequestIdGeneratorTest.class);

    @Test
    public  void testIdGenerators() throws InterruptedException {
        int iterations = 100;
        Set<Long> sortedIdGeneratedSet = Collections.synchronizedSortedSet(new TreeSet<Long>());
        final CountDownLatch countDownLatch = new CountDownLatch(iterations);

        long firstId = RequestIdGenerator.nextId()+1;
        for (long i = firstId; i < firstId+iterations; i++) {
            Runnable r = ()->{
                long id = RequestIdGenerator.nextId();
                sortedIdGeneratedSet.add(Long.valueOf(id));
                logger.info("Added id "+id);

                countDownLatch.countDown();
            };
            new Thread(r).start();
        }

        countDownLatch.await();


        logger.info("Set size "+sortedIdGeneratedSet.size());
        for (long i = firstId; i < firstId+iterations; i++) {
            assertTrue("requestId "+(i)+" not found in "+sortedIdGeneratedSet, sortedIdGeneratedSet.contains(new Long(i)));
        }
    }
}
