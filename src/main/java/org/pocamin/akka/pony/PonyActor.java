package org.pocamin.akka.pony;

import java.lang.reflect.InvocationTargetException;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

/**

 */
class PonyActor<U, T extends U> extends AbstractActor {
	private final LoggingAdapter log = Logging.getLogger(context().system(), this);

	PonyActor() {
		receive(ReceiveBuilder
				.match(MethodInvocation.class, this::call)
				.matchAny(m -> log.error("received unknown message " + m))
				.build()
		);

	}

	private void call(MethodInvocation methodInvocation) {
		try {
			FutureReturnImpl<Object> futureReturn = methodInvocation.invoke();
			futureReturn.completeOther(methodInvocation.getFutureReturn());
		}
		catch (InvocationTargetException e) {
			methodInvocation.getFutureReturn().completeOnError(e.getTargetException());
		}
		catch (IllegalAccessException e) {
			methodInvocation.getFutureReturn().completeOnError(e);
		}
	}
}
