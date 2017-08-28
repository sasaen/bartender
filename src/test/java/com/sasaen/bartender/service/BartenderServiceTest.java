package com.sasaen.bartender.service;

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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by santoss on 28/08/2017.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class BartenderServiceTest {

    @Autowired
    private BartenderService service;

    @Autowired
    private ActorUtil actorUtil;

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @Test
    public void getDrinksServed() throws ExecutionException, InterruptedException {


        final Props props = Props.create(DrinkRegistryActor.class);
        final TestActorRef<DrinkRegistryActor> registryActor = TestActorRef.create(system, props, "testRegistry");

        reset(actorUtil);
        expect(actorUtil.getRegistryActor()).andReturn(registryActor).anyTimes();
        replay(actorUtil);

        List<DrinkResponse> servedDrinks = service.getServedDrinks();
        assertNotNull(servedDrinks);
        assertEquals(0, servedDrinks.size());

        // Sends one drinks ready message
        DrinkRequestMessage drinkRequestMessage = new DrinkRequestMessage("A", DrinkType.DRINK);
        drinkRequestMessage.setDrinkStatus(DrinkRequestStatus.DRINK_READY);
        registryActor.tell(drinkRequestMessage, ActorRef.noSender());

        servedDrinks = service.getServedDrinks();
        assertNotNull(servedDrinks);
        assertEquals(1, servedDrinks.size());
    }





}
