package org.pocamin.akka.pony.performance;

import java.util.concurrent.CountDownLatch;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

/**

 */
public class PinkiePieActor extends AbstractActor {

	public PinkiePieActor(CountDownLatch latch) {
		receive(ReceiveBuilder.matchAny(m -> latch.countDown()).build());
	}
}
