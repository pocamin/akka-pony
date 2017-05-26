package org.pocamin.akka.pony;

import java.lang.reflect.Constructor;
import java.util.function.Function;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

/**

 */
public class Pony {

	public static <U, T extends U> T newPony(Class<T> clazz, ActorSystem actorSystem, Object... arguments) {
		return newPony(clazz, actorSystem::actorOf, arguments);
	}

	public static <U, T extends U> T newPony(Class<T> clazz, Actor parent, Object arguments) {
		return newPony(clazz, parent.context()::actorOf, arguments);
	}

	@SuppressWarnings("unchecked")
	public static <T> T newPony(Class<T> clazz, Function<Props, ActorRef> actorBuilder, Object... arguments) {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setSuperclass(clazz);

		try {
			T object = (T) findAppropriateConstructor(proxyFactory.createClass(), arguments).newInstance(arguments);
			((javassist.util.proxy.Proxy) object).setHandler(new PonyHandler(clazz, actorBuilder));
			return object;
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private static Constructor findAppropriateConstructor(Class clazz, Object[] args) throws NoSuchMethodException {
		Constructor toReturn = null;
		for (Constructor c : clazz.getConstructors()) {
			if (c.getParameterTypes().length == args.length && isAppropriateConstructor(c, args)) {
				if (toReturn == null) {
					toReturn = c;
				} else {
					throw new NoSuchMethodException(
							"There are multiple constructor matching given parameters. Ponies are not smart enough to find good one");
				}
			}
		}
		if (toReturn == null) {
			throw new NoSuchMethodException("There are no constructor matching given parameters.");
		}

		return toReturn;

	}

	private static boolean isAppropriateConstructor(Constructor<?> c, Object[] args) {
		for (int i = 0; i < args.length; i++) {
			if (!c.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
				return false;
			}
		}
		return true;
	}

	public static void kill(Object object) {
		kill(object, Safety.SAFE);
	}

	public static void kill(Object object, Safety Safety) {
		if (object instanceof ProxyObject) {
			ProxyObject proxyObject = (ProxyObject) object;
			((PonyHandler) proxyObject.getHandler()).kill(Safety);

		} else {
			throw new IllegalAccessError("object is not a proxy");
		}

	}

}
