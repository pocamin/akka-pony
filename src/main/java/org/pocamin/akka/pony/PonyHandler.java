package org.pocamin.akka.pony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import javassist.util.proxy.MethodHandler;

/**

 */
public class PonyHandler<T extends U, U> implements MethodHandler {

	private final Set<Method> filteredMethod;
	private final ActorRef actor;
	private boolean isKilled = false;
	private Safety killSafety = Safety.SAFE;

	PonyHandler(Class<U> clazz, Function<Props, ActorRef> actorBuilder) {
		this.filteredMethod = Stream.of(clazz.getMethods())
				.filter(m -> FutureReturn.class.isAssignableFrom(m.getReturnType()))
				.collect(Collectors.toSet());

		actor = actorBuilder.apply(Props.create(PonyActor.class, PonyActor::new));
	}

	@Override
	public Object invoke(Object self, Method calledMethod, Method original, Object[] args) throws Throwable {

		if (filteredMethod.contains(calledMethod)) {
			if (!isKilled) {
				MethodInvocation methodInvocation = new MethodInvocation() {
					@SuppressWarnings("unchecked")
					@Override
					FutureReturnImpl<Object> invoke() throws InvocationTargetException, IllegalAccessException {
						return (FutureReturnImpl<Object>) original.invoke(self, args);
					}
				};
				actor.tell(methodInvocation, ActorRef.noSender());

				return methodInvocation.getFutureReturn();
			}
			if (killSafety == Safety.SAFE) {
				return FutureReturn.error(new PonyAlreadyKilledException(self));
			}
		}

		return original.invoke(self, args);  // execute the original method.
	}

	public void kill(Safety safety) {
		isKilled = true;
		this.killSafety = safety;
		actor.tell(PoisonPill.getInstance(), ActorRef.noSender());

	}

}
