package com.sasaen.bartender.service;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.sasaen.bartender.actors.DrinkRegistryActor;
import com.sasaen.bartender.request.RequestIdGenerator;
import com.sasaen.bartender.actors.ActorUtil;
import com.sasaen.bartender.actors.DrinkRequestMessage;
import com.sasaen.bartender.enums.DrinkStatus;
import com.sasaen.bartender.enums.DrinkType;
import com.sasaen.bartender.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by santoss on 26/08/2017.
 */
@Service("bartenderService")
public class BartenderService {

    @Autowired
    private ActorUtil actorUtil;


    @Value("${drink.request.timeout.milliseconds}")
    private Integer drinkRequestTimeout;

    public static Logger logger = LoggerFactory.getLogger(BartenderService.class);

    public List<Result> getServedDrinks() {
        Timeout requestDrinkTimeout = new Timeout(drinkRequestTimeout, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(actorUtil.getRegistryActor(), DrinkRegistryActor.DrinkRegistryMessage.DRINK_SERVED_LIST,
                requestDrinkTimeout);

        try {
            List<DrinkRequestMessage> result = (List<DrinkRequestMessage>) Await.result(future, requestDrinkTimeout.duration());

            // A bit of stream and method reference
            return result.stream().map(DrinkRequestMessage::toResult).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Exception getting served drinks from requestRegistryActor= " + e.getMessage());
        }
        return null;
    }

    public Result requestDrink(String customerId, String drinkType) {

        Assert.notNull(customerId, "customerId must not be null");
        Assert.notNull(drinkType, "drinkType must not be null");
        DrinkType drinkTypeEnum = DrinkType.getDrinkType(drinkType);
        long requestId = RequestIdGenerator.nextId();

        DrinkRequestMessage drinkRequestMessage = new DrinkRequestMessage(customerId, requestId, drinkTypeEnum);
        ActorRef requestDrinkActor = actorUtil.getActor("drinkRequestActor", "drinkRequestActor_" + requestId);

        Timeout requestDrinkTimeout = new Timeout(drinkRequestTimeout, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(requestDrinkActor, drinkRequestMessage, requestDrinkTimeout);
        DrinkRequestMessage result;
        try {
            result = (DrinkRequestMessage) Await.result(future, requestDrinkTimeout.duration());
            if (result.getDrinkStatus() == DrinkStatus.DRINK_REQUEST_ACCEPTED) {
                logger.info("**** accepted drink request acknowledge= " + result);
                result.setDrinkStatus(DrinkStatus.DRINK_REQUEST_ACCEPTED_ACK);
                requestDrinkActor.tell(drinkRequestMessage, ActorRef.noSender());

                return getResult(customerId, drinkType, true);
            }
            sendTimeoutMessage(requestDrinkActor, drinkRequestMessage);
        } catch (TimeoutException e) {
            sendTimeoutMessage(requestDrinkActor, drinkRequestMessage);
        } catch (Exception e) {
            logger.error("Exception requestDrinkActor " + drinkRequestMessage + " " + e.getMessage());
        }

        return getResult(customerId, drinkType, false);

    }

    private Result getResult(String customerId, String drinkType, boolean requestAccepted) {
        Result result = new Result();
        result.setCustomer(customerId);
        result.setDrinkType(drinkType);
        result.setRequestAccepted(requestAccepted);
        return result;
    }

    private void sendTimeoutMessage(ActorRef requestDrinkActor, DrinkRequestMessage drinkRequestMessage) {
        drinkRequestMessage.setDrinkStatus(DrinkStatus.DRINK_REQUEST_TIMEOUT);
        requestDrinkActor.tell(drinkRequestMessage, ActorRef.noSender());
        logger.warn("**** timeout drink request " + drinkRequestMessage);
    }
}
