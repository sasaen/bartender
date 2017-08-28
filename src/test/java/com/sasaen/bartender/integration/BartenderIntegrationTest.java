package com.sasaen.bartender.integration;

import akka.actor.ActorRef;
import com.sasaen.bartender.actors.ActorUtil;
import com.sasaen.bartender.actors.DrinkDispatcherActor;
import com.sasaen.bartender.response.DrinkResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BartenderIntegrationTest {

    @LocalServerPort
    private int port;


    @Autowired
    private ConfigurableApplicationContext context;
    private TestRestTemplate restTemplate = new TestRestTemplate();

    public static Logger logger = LoggerFactory.getLogger(BartenderIntegrationTest.class);
    private static boolean init = false;

    @Before
    public void setup() {
        if (init)
            return;

        init = true;

        ActorUtil actorUtil = context.getBean(ActorUtil.class);
        actorUtil.registerSingletonActors();
    }

    @Test
    public void requestDrinkNoWait() throws Exception {
        Map<Boolean, Integer> resultInvocationCountMap = new HashMap<>();
        int numberOfInvocations = 700;
        CountDownLatch countDownLatch = new CountDownLatch(numberOfInvocations);

        logger.info("Launching  "+numberOfInvocations+" drink requests");
        for (int i = 0; i < numberOfInvocations/2; i++) {
            requestDrinkNoWait("C", "BEER", countDownLatch, resultInvocationCountMap);
            requestDrinkNoWait("D", "DRINK", countDownLatch, resultInvocationCountMap);

            // Distribute the calls in time a bit
            if (i%20==0){
                Thread.sleep(500);
            }
        }

        countDownLatch.await();

        logger.info("Served drinks "+resultInvocationCountMap.get(Boolean.TRUE));
        logger.info("Timeout drinks "+resultInvocationCountMap.get(Boolean.FALSE));

        assertEquals(2, resultInvocationCountMap.size());
        assertEquals(numberOfInvocations, resultInvocationCountMap.get(Boolean.FALSE) + resultInvocationCountMap.get(Boolean.TRUE));
        assertTrue(resultInvocationCountMap.get(Boolean.FALSE) > resultInvocationCountMap.get(Boolean.TRUE));

        logger.info("Waiting for the bartender to be freed up and show dispatcher status");
        Thread.sleep(5000);

        ActorUtil actorUtil = context.getBean(ActorUtil.class);
        actorUtil.getDispatcherActor().tell(DrinkDispatcherActor.InternalMessage.PRINT_INTERNAL_STATUS, ActorRef.noSender());

        Thread.sleep(5000);
    }

    @Test
    public void requestDrinkAndWait() throws Exception {
        requestDrinkAndWait("A", "BEER", HttpStatus.OK);
        requestDrinkAndWait("A", "BEER", HttpStatus.OK);
        requestDrinkAndWait("B", "DRINK", HttpStatus.TOO_MANY_REQUESTS);

        Thread.sleep(5000);

        requestDrinkAndWait("A", "BEER", HttpStatus.OK);
        requestDrinkAndWait("A", "DRINK", HttpStatus.TOO_MANY_REQUESTS);
        requestDrinkAndWait("B", "BEER", HttpStatus.OK);

        Thread.sleep(2500);
        requestDrinkAndWait("A", "DRINK", HttpStatus.TOO_MANY_REQUESTS);

        Thread.sleep(1000);
        requestDrinkAndWait("A", "DRINK", HttpStatus.OK);
        requestDrinkAndWait("A", "BEER", HttpStatus.TOO_MANY_REQUESTS);

        logger.info("Waiting for the bartender to be freed up");
        Thread.sleep(5000);
    }

    private void requestDrinkNoWait(final String customer, final String drinkType, CountDownLatch countDownLatch,
                                    Map<Boolean, Integer> resultInvocationCountMap) throws InterruptedException {
        Runnable runnable = () -> {
            ResponseEntity<DrinkResponse> responseEntity =
                    restTemplate.exchange("http://localhost:" + port + "/bartender/request/" + customer + "/" + drinkType,
                            HttpMethod.POST, null, new ParameterizedTypeReference<DrinkResponse>() {
                            });
            Boolean success = responseEntity.getStatusCode() == HttpStatus.OK;

            recordCountResult(resultInvocationCountMap, success);

            responseEntity.getStatusCode();
            countDownLatch.countDown();
        };

        new Thread(runnable).start();
    }

    private synchronized void recordCountResult(Map<Boolean, Integer> resultInvocationMap, Boolean success) {
        Integer count = resultInvocationMap.get(success);
        if (count == null) {
            resultInvocationMap.put(success, new Integer(0));
            count = resultInvocationMap.get(success);
        }
        resultInvocationMap.put(success, ++count);
    }

    private void requestDrinkAndWait(String customer, String drinkType, HttpStatus expectedResult) {
        ResponseEntity<DrinkResponse> responseEntity =
                restTemplate.exchange("http://localhost:" + port + "/bartender/request/" + customer + "/" + drinkType,
                        HttpMethod.POST, null, new ParameterizedTypeReference<DrinkResponse>() {
                        });

        assertEquals(expectedResult, responseEntity.getStatusCode());
    }

    @Test
    public void getServedDrinks() throws InterruptedException {
        logger.info("Waiting for the bartender to be freed up");
        Thread.sleep(5000);
        requestDrinkAndWait("served_drink_test", "DRINK", HttpStatus.OK);

        logger.info("Waiting for the bartender to serve the drink");
        Thread.sleep(5000);

        ResponseEntity<List<DrinkResponse>> responseEntity =
                restTemplate.exchange("http://localhost:" + port + "/bartender/served-drinks/",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<DrinkResponse>>() {
                        });

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().size() > 0 );

        assertEquals(1, responseEntity.getBody().stream().
                filter(r-> (r.getCustomer().equals("served_drink_test")&& r.getDrinkType().equals("DRINK") )).count());
    }

}
