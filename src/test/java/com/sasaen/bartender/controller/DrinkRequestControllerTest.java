package com.sasaen.bartender.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import com.sasaen.bartender.actors.ActorUtil;
import com.sasaen.bartender.actors.DrinkRegistryActor;
import com.sasaen.bartender.actors.DrinkRequestMessage;
import com.sasaen.bartender.enums.DrinkRequestStatus;
import com.sasaen.bartender.enums.DrinkType;
import com.sasaen.bartender.response.DrinkResponse;
import com.sasaen.bartender.service.BartenderService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

/**
 * Created by santoss on 28/08/2017.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class DrinkRequestControllerTest {

    @Autowired
    private BartenderService service;

    @Autowired
    private DrinkRequestController controller;


    @Test
    public void getDrinksServed() throws ExecutionException, InterruptedException {

        reset(service);
        expect(service.getServedDrinks()).andReturn(new ArrayList<DrinkResponse>());
        expect(service.getServedDrinks()).andReturn(Arrays.asList(new DrinkResponse()));
        replay(service);

        // first call, no drinks
        ResponseEntity<List<DrinkResponse>> servedDrinks =  controller.getServedDrinks();
        assertNotNull(servedDrinks);
        assertEquals(HttpStatus.OK, servedDrinks.getStatusCode());
        assertNotNull(servedDrinks.getBody());
        assertEquals(0, servedDrinks.getBody().size());

        // second call, 1 drink
        servedDrinks =  controller.getServedDrinks();
        assertNotNull(servedDrinks);
        assertEquals(HttpStatus.OK, servedDrinks.getStatusCode());
        assertNotNull(servedDrinks.getBody());
        assertEquals(1, servedDrinks.getBody().size());

    }

    @Test
    public void requestDrink() throws ExecutionException, InterruptedException {

        DrinkResponse responseOK= new DrinkResponse();
        responseOK.setRequestAccepted(true);

        DrinkResponse responseTimeout= new DrinkResponse();
        responseTimeout.setRequestAccepted(false);

        reset(service);
        expect(service.requestDrink("A", "DRINK")).andReturn(responseOK);
        expect(service.requestDrink("B", "BEER")).andReturn(responseTimeout);
        replay(service);

        // call for B and BEER, mock timeout
        ResponseEntity<?> responseEntityTimeout = controller.requestDrink("B", "BEER");
        assertNotNull(responseEntityTimeout);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, responseEntityTimeout.getStatusCode());
        assertNull(responseEntityTimeout.getBody());


        // call for B and BEER, mock timeout
        ResponseEntity<?> responseEntityOK = controller.requestDrink("A", "DRINK");
        assertNotNull(responseEntityOK);
        assertEquals(HttpStatus.OK, responseEntityOK.getStatusCode());
        assertNull(responseEntityOK.getBody());
    }
}
