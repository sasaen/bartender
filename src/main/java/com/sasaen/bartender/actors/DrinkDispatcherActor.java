package com.sasaen.bartender.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sasaen.bartender.enums.DrinkStatus.*;

/**
 * Created by santoss on 25/08/2017.
 */
@Component("drinkDispatcherActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DrinkDispatcherActor extends UntypedActor {

    public final static String DRINK_DISPATCHER_ACTOR_TYPE ="drinkDispatcherActor";
    public final static String DRINK_DISPATCHER_ACTOR_NAME="drinkDispatcherSingletonActor";


    @Autowired
    private ActorUtil actorUtil;

    @Value("${drink.dispatcher.capacity}")
    private int capacity;

    private List<ActorRef> availableDrinkWorkers = new ArrayList<>(capacity);
    private Map<DrinkRequestMessage, ActorRef> allocatedDrinkWorkers = new HashMap<>(capacity);
    private Map<DrinkRequestMessage, ActorRef> requestBeingServedActors = new HashMap<>();
    private Map<DrinkRequestMessage, ActorRef> requestWaitingActors = new HashMap<>();

    public static Logger logger = LoggerFactory.getLogger(DrinkDispatcherActor.class);

    @Override
    public void preStart() {
        logger.debug("creating workers " + capacity + " " + this);
        for (int i = 0; i < capacity; i++) {
            availableDrinkWorkers.add(actorUtil.getActor(DrinkWorker.DRINK_WORKER_TYPE, DrinkWorker.DRINK_WORKER_TYPE + i));
        }
    }

    public void onReceive(Object message) {

        if (message instanceof DrinkRequestMessage) {
            DrinkRequestMessage drink = (DrinkRequestMessage) message;

            if (drink.getDrinkStatus() == DRINK_REQUESTED) {
                ActorRef availableWorker = getAvailableWorker(drink);
                if (availableWorker == null) {
                    logger.debug("no drink maker available= " + drink);
                    drink.setDrinkStatus(WAIT_TO_BE_SERVED);
                    getSender().tell(drink, getSelf());
                    requestWaitingActors.put(drink, getSender());
                } else {
                    logger.debug("drink request approved= " + drink);
                    drink.setDrinkStatus(DRINK_REQUEST_ACCEPTED);
                    getSender().tell(drink, getSelf());
                    allocateWorker(availableWorker, drink);
                }
            } else if (drink.getDrinkStatus() == DRINK_REQUEST_ACCEPTED_ACK) {
                logger.debug("worker to prepare drink= " + drink);
                ActorRef allocatedWorker = allocatedDrinkWorkers.get(drink);
                allocatedWorker.tell(drink, getSelf());

                requestBeingServedActors.put(drink, getSender());
            } else if (drink.getDrinkStatus() == DRINK_READY) {
                logger.debug("dispatched drink= " + drink);
                deallocateWorker(getSender(), drink);

                requestBeingServedActors.remove(drink);

                notifyWaitingActors();

                notifyRegistry(message);
            } else if (drink.getDrinkStatus() == DRINK_REQUEST_TIMEOUT) {
                logger.debug("handling tired of waiting= " + drink);
                clearWaitingActor(drink);
            } else {
                unhandled(message);
            }
        } else {
            unhandled(message);
        }
    }

    private void notifyRegistry(Object message) {
        actorUtil.getRegistryActor().tell(message, getSelf());
    }

    private void clearWaitingActor(DrinkRequestMessage drink) {
        requestWaitingActors.remove(drink, getSender());
    }

    private void notifyWaitingActors() {
        for (Map.Entry<DrinkRequestMessage, ActorRef> waitingActorEntry : requestWaitingActors.entrySet()) {
            DrinkRequestMessage drinkWaiting = waitingActorEntry.getKey();
            drinkWaiting.setDrinkStatus(DRINK_WORKER_AVAILABLE);
            waitingActorEntry.getValue().tell(drinkWaiting, getSelf());
        }
        requestWaitingActors.clear();
    }

    private ActorRef getAvailableWorker(DrinkRequestMessage drink) {
        if (capacity >= drink.getDrinkType().getEffort()) {
            return availableDrinkWorkers.get(0);
        }
        return null;
    }

    private void allocateWorker(ActorRef availableWorker, DrinkRequestMessage drink) {
        allocatedDrinkWorkers.put(drink, availableWorker);
        availableDrinkWorkers.remove(availableWorker);
        capacity = capacity - drink.getDrinkType().getEffort();
    }

    private void deallocateWorker(ActorRef allocatedWorker, DrinkRequestMessage drink) {
        allocatedDrinkWorkers.remove(drink, allocatedWorker);
        availableDrinkWorkers.add(allocatedWorker);
        capacity += drink.getDrinkType().getEffort();
    }
}
