package org.pocamin.akka.pony;

import java.lang.reflect.InvocationTargetException;

abstract class MethodInvocation {
	private final FutureReturnImpl<Object> futureReturn;

	MethodInvocation() {
		futureReturn = new FutureReturnImpl<>();
	}

	FutureReturnImpl<Object> getFutureReturn() {
		return futureReturn;
	}

	abstract FutureReturnImpl<Object> invoke() throws InvocationTargetException, IllegalAccessException;
}