package com.sasaen.bartender.service;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.sasaen.bartender.actors.ActorUtil;
import com.sasaen.bartender.actors.DrinkRegistryActor;
import com.sasaen.bartender.actors.DrinkRequestMessage;
import com.sasaen.bartender.enums.DrinkRequestStatus;
import com.sasaen.bartender.enums.DrinkType;
import com.sasaen.bartender.response.DrinkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * This is the entry point to communicate with the Actors in charge of handling Drink requests.
 * <p>
 * Created by santoss on 26/08/2017.
 */
@Service("bartenderService")
public class BartenderServiceImpl implements BartenderService{

    @Autowired
    private ActorUtil actorUtilImpl;

    @Value("${drink.request.timeout.milliseconds}")
    private Integer drinkRequestTimeout;

    public static Logger logger = LoggerFactory.getLogger(BartenderServiceImpl.class);

    /**
     * Asks the DrinkRegistry the list of <code>DrinkRequestMessage</code>. and transforms it to a list of <code>DrinkResponse</code>.
     *
     * @return the List of served drinks.
     */
    public List<DrinkResponse> getServedDrinks() {
        Timeout requestDrinkTimeout = new Timeout(drinkRequestTimeout, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(actorUtilImpl.getRegistryActor(), DrinkRegistryActor.DrinkRegistryMessage.DRINK_SERVED_LIST,
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

    /**
     * Requests a drink to a DrinkRequestActor and waits a number of milliseconds for the response.
     * If a DRINK_REQUEST_ACCEPTED is received then a DRINK_REQUEST_ACCEPTED_ACK message is sent.
     * <p>
     * If there is a timeout or the response is not DRINK_REQUEST_ACCEPTED then it sends a DRINK_REQUEST_TIMEOUT
     * to free resources.
     *
     * @param customer  - The customer name.
     * @param drinkType - The drink type string. Possible valid values: BEER and DRINK, case sensitive.
     * @return the response to the request.
     */
    public DrinkResponse requestDrink(String customer, String drinkType) {

        Assert.notNull(customer, "customer must not be null");
        Assert.notNull(drinkType, "drinkType must not be null");
        DrinkType drinkTypeEnum = DrinkType.getDrinkType(drinkType);

        DrinkRequestMessage drinkRequestMessage = new DrinkRequestMessage(customer, drinkTypeEnum);
        ActorRef requestDrinkActor = actorUtilImpl.getActor("drinkRequestActor", "drinkRequestActor_" + drinkRequestMessage.getRequestId());

        Timeout requestDrinkTimeout = new Timeout(drinkRequestTimeout, TimeUnit.MILLISECONDS);
        Future<Object> future = Patterns.ask(requestDrinkActor, drinkRequestMessage, requestDrinkTimeout);
        DrinkRequestMessage result;
        try {
            result = (DrinkRequestMessage) Await.result(future, requestDrinkTimeout.duration());
            if (result.getDrinkStatus() == DrinkRequestStatus.DRINK_REQUEST_ACCEPTED) {
                logger.info("**** accepted drink request acknowledge= " + result);

                result.setDrinkStatus(DrinkRequestStatus.DRINK_REQUEST_ACCEPTED_ACK);
                requestDrinkActor.tell(drinkRequestMessage, ActorRef.noSender());
                return getResult(customer, drinkType, true);
            }
            sendTimeoutMessage(requestDrinkActor, drinkRequestMessage);
        } catch (TimeoutException e) {
            sendTimeoutMessage(requestDrinkActor, drinkRequestMessage);
        } catch (Exception e) {
            logger.error("Exception requestDrinkActor " + drinkRequestMessage + " " + e.getMessage());
            sendTimeoutMessage(requestDrinkActor, drinkRequestMessage);
        }

        return getResult(customer, drinkType, false);

    }

    private DrinkResponse getResult(String customerId, String drinkType, boolean requestAccepted) {
        DrinkResponse result = new DrinkResponse();
        result.setCustomer(customerId);
        result.setDrinkType(drinkType);
        result.setRequestAccepted(requestAccepted);
        return result;
    }

    private void sendTimeoutMessage(ActorRef requestDrinkActor, DrinkRequestMessage drinkRequestMessage) {
        drinkRequestMessage.setDrinkStatus(DrinkRequestStatus.DRINK_REQUEST_TIMEOUT);
        requestDrinkActor.tell(drinkRequestMessage, ActorRef.noSender());
        logger.warn("**** timeout drink request " + drinkRequestMessage);
    }
}
