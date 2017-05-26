package org.pocamin.akka.pony.performance;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

/**

 */
public class TwilightSparkleActor extends AbstractActor {
	public TwilightSparkleActor( ActorRef twilightSparkle) {
		receive(ReceiveBuilder.matchAny((m) -> twilightSparkle.tell("ping", self())).build());
	}
}
