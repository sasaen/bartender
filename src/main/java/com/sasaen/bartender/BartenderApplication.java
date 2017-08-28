package com.sasaen.bartender;

import com.sasaen.bartender.actors.ActorUtilImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BartenderApplication {

    public static void main(String[] args) {

        final ConfigurableApplicationContext applicationContext = SpringApplication.run(BartenderApplication.class, args);

        // Registers singleton actors, could not get another way to inject them using Spring
        // TODO find a way create an actor and reference it using actorSystem.actorSelection(path)
        ActorUtilImpl actorUtilImpl = applicationContext.getBean(ActorUtilImpl.class);
        actorUtilImpl.registerSingletonActors();
    }


}
