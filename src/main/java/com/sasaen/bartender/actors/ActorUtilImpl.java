package com.sasaen.bartender.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sasaen.bartender.akka.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Utility class to handle the Actor creation.
 * It also registers in the Spring container 2 singleton actors: Dispatcher and Registry.
 *
 * Created by santoss on 26/08/2017.
 */
@Component
public class ActorUtilImpl implements ActorUtil {

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private ConfigurableApplicationContext context;

    public ActorRef getActor(String actorType, String actorName) {
        return actorSystem.actorOf(
                SpringExtension.SpringExtProvider.get(actorSystem).props(actorType), actorName);
    }

    public ActorRef getRegistryActor(){
        return (ActorRef) context.getBean(DrinkRegistryActor.DRINK_REGISTRY_ACTOR_NAME);
    }

    public ActorRef getDispatcherActor(){
        return (ActorRef) context.getBean(DrinkDispatcherActor.DRINK_DISPATCHER_ACTOR_NAME);
    }

    public void registerSingletonActors(){
        ActorRef dispatcherActor = getActor(DrinkDispatcherActor.DRINK_DISPATCHER_ACTOR_TYPE, DrinkDispatcherActor.DRINK_DISPATCHER_ACTOR_NAME);
        context.getBeanFactory().registerSingleton(DrinkDispatcherActor.DRINK_DISPATCHER_ACTOR_NAME, dispatcherActor);

        ActorRef registryActor = getActor(DrinkRegistryActor.DRINK_REGISTRY_ACTOR_TYPE, DrinkRegistryActor.DRINK_REGISTRY_ACTOR_NAME);
        context.getBeanFactory().registerSingleton(DrinkRegistryActor.DRINK_REGISTRY_ACTOR_NAME, registryActor);
    }


}
