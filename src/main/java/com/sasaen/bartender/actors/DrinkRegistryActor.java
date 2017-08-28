package com.sasaen.bartender.actors;

import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

import static com.sasaen.bartender.enums.DrinkRequestStatus.DRINK_READY;

/**
 * This actor keeps  a list of the prepared drinks.
 *
 * Created by santoss on 27/08/2017.
 */
@Component("drinkRegistryActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DrinkRegistryActor extends UntypedActor {

    public final static String DRINK_REGISTRY_ACTOR_TYPE ="drinkRegistryActor";
    public final static String DRINK_REGISTRY_ACTOR_NAME="drinkRegistrySingletonActor";

    public enum DrinkRegistryMessage {
        DRINK_SERVED_LIST
    }

    private List<DrinkRequestMessage> servedDrinks = new LinkedList<>();

    public static Logger logger = LoggerFactory.getLogger(DrinkRegistryActor.class);

    public void onReceive(Object message) {

        if (message instanceof DrinkRequestMessage) {
            DrinkRequestMessage drink = (DrinkRequestMessage) message;

            if (drink.getDrinkStatus() == DRINK_READY) {
                logger.info("**** register drink ready "+drink);
                servedDrinks.add(drink);
            } else {
                unhandled(message);
            }
        } else if (message instanceof DrinkRegistryMessage) {
            logger.info("**** return drink served list, size= "+servedDrinks.size());
            getSender().tell(servedDrinks, getSelf());
        } else {
            unhandled(message);
        }
    }


}
