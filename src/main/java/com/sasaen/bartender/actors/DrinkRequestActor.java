package com.sasaen.bartender.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.sasaen.bartender.enums.DrinkRequestStatus.*;
/**
 * This actor handles the multiple requests between the BartenderService and the unique Dispatcher actor.
 *
 * Created by santoss on 25/08/2017.
 */
@Component("drinkRequestActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DrinkRequestActor extends UntypedActor {


    @Autowired
    private ActorUtil actorUtil;

    private ActorRef requestActor;

    public static Logger logger = LoggerFactory.getLogger(DrinkRequestActor.class);

    public void onReceive(Object message) {
        if (message instanceof DrinkRequestMessage) {
            DrinkRequestMessage drink = (DrinkRequestMessage) message;
            if (drink.getDrinkStatus() == DRINK_REQUESTED) {
                logger.debug("communicating request drink= " + drink);

                requestActor = getSender();
                actorUtil.getDispatcherActor().tell(drink, getSelf());
            } else if (drink.getDrinkStatus() == DRINK_REQUEST_ACCEPTED_ACK) {
                logger.debug("communicating request approved ack drink= " + drink);

                requestActor = getSender();
                actorUtil.getDispatcherActor().tell(drink, getSelf());
            } else if (drink.getDrinkStatus() == DRINK_REQUEST_ACCEPTED) {
                logger.debug("communicating drink request approved= " + drink);

                requestActor.tell(drink, getSelf());
            } else if (drink.getDrinkStatus() == WAIT_TO_BE_SERVED) {
                // Wait until a DRINK_WORKER_AVAILABLE message is received
                logger.info("**** wait to be served= " + drink);
            } else if (drink.getDrinkStatus() == DRINK_REQUEST_TIMEOUT) {
                logger.debug("communicating tired of waiting= " + drink);

                actorUtil.getDispatcherActor().tell(message, getSelf());
            } else if (drink.getDrinkStatus() == DRINK_WORKER_AVAILABLE) {
                logger.debug("communicating drink worker available= " + drink);

                // Send the request again
                drink.setDrinkStatus(DRINK_REQUESTED);
                actorUtil.getDispatcherActor().tell(drink, getSelf());
            } else {
                unhandled(message);
            }
        } else {
            unhandled(message);
        }
    }

}
