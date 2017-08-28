package com.sasaen.bartender.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import com.sasaen.bartender.enums.DrinkRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by santoss on 25/08/2017.
 */
@Component("drinkWorker")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DrinkWorker extends UntypedActor {

    public final static String DRINK_WORKER_TYPE = "drinkWorker";

    @Value("${drink.preparation.time.milliseconds}")
    private Integer drinkPreparationTime;

    public static Logger logger = LoggerFactory.getLogger(DrinkWorker.class);

    public void onReceive(Object message) {
        if (message instanceof DrinkRequestMessage) {
            DrinkRequestMessage drink = (DrinkRequestMessage) message;


            logger.debug("preparing drink= " + drink);
            try {
                Thread.sleep(drinkPreparationTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("**** prepared drink= " + drink);
            drink.setDrinkStatus(DrinkRequestStatus.DRINK_READY);
            getSender().tell(drink, getSelf());
        } else {
            unhandled(message);
        }
    }

    static public Props props() {
        return Props.create(DrinkWorker.class, () -> new DrinkWorker());
    }
}
