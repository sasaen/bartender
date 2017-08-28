package com.sasaen.bartender;

import com.sasaen.bartender.actors.ActorUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BartenderApplication {

    public static void main(String[] args) {

        final ConfigurableApplicationContext applicationContext = SpringApplication.run(BartenderApplication.class, args);

        // Registers singleton actors, could not get another way to inject them using Spring
        // TODO find a way create an actor and reference it using actorSystem.actorSelection(path)
        ActorUtil actorUtil = applicationContext.getBean(ActorUtil.class);
        actorUtil.registerSingletonActors();
    }


}
