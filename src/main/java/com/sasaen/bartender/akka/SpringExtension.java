package com.sasaen.bartender.akka;


import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by santoss on 26/08/2017.
 */
@Component
public class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExt> {

    public static SpringExtension SpringExtProvider = new SpringExtension();

    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    public static class SpringExt implements Extension {
        private volatile ApplicationContext applicationContext;

        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        public Props props(String actorBeanName) {
            return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
        }
    }
}
