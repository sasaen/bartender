package com.sasaen.bartender.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.sasaen.bartender.akka.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Interface class to handle the Actor creation.
 * <p>
 * Created by santoss on 28/08/2017.
 */
public interface ActorUtil {


    ActorRef getActor(String actorType, String actorName);

    ActorRef getRegistryActor();

    ActorRef getDispatcherActor();

    void registerSingletonActors();
}
