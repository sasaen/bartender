package com.sasaen.bartender.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sasaen.bartender.enums.DrinkRequestStatus.*;

/**
 * This actor:
 * <ul>
 * <li>receives the drink request messages</li>
 * <li>check/allocate/deallocate the available workers</li>
 * <li>instruct the workers to prepare drinks</li>
 * <li>communicate to the registry the prepared drinks</li>
 * </ul>
 * <p>
 * Created by santoss on 25/08/2017.
 */
@Component("drinkDispatcherActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DrinkDispatcherActor extends UntypedActor {

    public final static String DRINK_DISPATCHER_ACTOR_TYPE = "drinkDispatcherActor";
    public final static String DRINK_DISPATCHER_ACTOR_NAME = "drinkDispatcherSingletonActor";

    /**
     * Interested in printing the internal status for testing purposes
     */
    public enum InternalMessage {
        PRINT_INTERNAL_STATUS
    }

    @Autowired
    private ActorUtilImpl actorUtilImpl;

    @Value("${drink.dispatcher.capacity}")
    private int capacity;

    // List of available worker actors.
    private List<ActorRef> availableDrinkWorkers = new ArrayList<>(capacity);

    // Thew workers are allocated before receiving an DRINK_REQUEST_ACCEPTED_ACK and deallocated on DRINK_READY OR DRINK_REQUEST_TIMEOUT
    private Map<DrinkRequestMessage, ActorRef> allocatedDrinkWorkers = new HashMap<>(capacity);

    // Request actors being served
    private Map<DrinkRequestMessage, ActorRef> beingServedActors = new HashMap<>();

    // Request actors waiting to be served
    private Map<DrinkRequestMessage, ActorRef> waitingActors = new HashMap<>();

    public static Logger logger = LoggerFactory.getLogger(DrinkDispatcherActor.class);

    /**
     * Add a number of workers equals to the capacity configured.
     */
    @Override
    public void preStart() {
        logger.debug("creating workers " + capacity + " " + this);
        for (int i = 0; i < capacity; i++) {
            availableDrinkWorkers.add(actorUtilImpl.getActor(DrinkWorker.DRINK_WORKER_TYPE, DrinkWorker.DRINK_WORKER_TYPE + i));
        }
    }

    @Override
    public void onReceive(Object message) {
        logger.debug("dispatcher status " + this + " before handling message=  " + message);
        if (message instanceof DrinkRequestMessage) {
            DrinkRequestMessage drink = (DrinkRequestMessage) message;

            if (drink.getDrinkStatus() == DRINK_REQUESTED) {
                ActorRef availableWorker = getAvailableWorker(drink);
                if (availableWorker == null) {
                    logger.debug("no drink maker available= " + drink);

                    drink.setDrinkStatus(WAIT_TO_BE_SERVED);
                    getSender().tell(drink, getSelf());
                    waitingActors.put(drink, getSender());
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
                beingServedActors.put(drink, getSender());
            } else if (drink.getDrinkStatus() == DRINK_READY) {
                logger.debug("dispatched drink= " + drink);

                deallocateWorker(getSender(), drink);
                beingServedActors.remove(drink);
                notifyWaitingActors();
                notifyRegistry(message);
            } else if (drink.getDrinkStatus() == DRINK_REQUEST_TIMEOUT) {
                logger.debug("handling drink= " + drink);

                clearWaitingActor(drink);
                deallocateWorker(getSender(), drink);
            } else {
                unhandled(message);
            }
        } else if (message instanceof InternalMessage && message == InternalMessage.PRINT_INTERNAL_STATUS) {
            logger.info("dispatcher internal status= " + this);
        } else {
            unhandled(message);
        }
    }

    private void notifyRegistry(Object message) {
        actorUtilImpl.getRegistryActor().tell(message, getSelf());
    }

    private void clearWaitingActor(DrinkRequestMessage drink) {
        waitingActors.remove(drink, getSender());
    }

    /**
     * Notify to all the waiting actors that there is a worker available.
     */
    private void notifyWaitingActors() {
        for (Map.Entry<DrinkRequestMessage, ActorRef> waitingActorEntry : waitingActors.entrySet()) {
            DrinkRequestMessage drinkWaiting = waitingActorEntry.getKey();
            drinkWaiting.setDrinkStatus(DRINK_WORKER_AVAILABLE);
            waitingActorEntry.getValue().tell(drinkWaiting, getSelf());
        }
        waitingActors.clear();
    }

    /**
     *
     * @param drink - The drink to check if there is an available worker.
     * @return a worker if the capacity is equal or higher than the drink effort, otherwise null.
     */
    private ActorRef getAvailableWorker(DrinkRequestMessage drink) {
        if (capacity >= drink.getDrinkType().getEffort()) {
            return availableDrinkWorkers.get(0);
        }
        return null;
    }

    /**
     * Allocate the  worker for the given drink and reduce the capapcity based on the drink effort.
     * @param availableWorker - The worker to be allocated
     * @param drink - the drink to be prepared
     */
    private void allocateWorker(ActorRef availableWorker, DrinkRequestMessage drink) {
        allocatedDrinkWorkers.put(drink, availableWorker);
        availableDrinkWorkers.remove(availableWorker);
        capacity = capacity - drink.getDrinkType().getEffort();
    }

    /**
     * Deallocated the worker for the drink.
     * @param allocatedWorker - the worker to be deallocated.
     * @param drink - the drink to deallocate a worker.
     */
    private void deallocateWorker(ActorRef allocatedWorker, DrinkRequestMessage drink) {
        ActorRef deallocatedWorker = allocatedDrinkWorkers.remove(drink);
        if (deallocatedWorker != null) {
            availableDrinkWorkers.add(allocatedWorker);
            capacity += drink.getDrinkType().getEffort();
        }
    }

    @Override
    public String toString() {
        return "DrinkDispatcherActor{" +
                " capacity=" + capacity +
                ", availableDrinkWorkers=" + availableDrinkWorkers.size() +
                ", allocatedDrinkWorkers=" + allocatedDrinkWorkers.size() +
                ", beingServedActors=" + beingServedActors.size() +
                ", waitingActors=" + waitingActors.size() +
                "} " ;
    }
}
