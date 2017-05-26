package org.pocamin.akka.pony;

/**
 * (c) Swissquote 2017/05/26
 */
public class PonyAlreadyKilledException extends RuntimeException {

	private final Object pony;

	public PonyAlreadyKilledException(Object pony) {
		this.pony = pony;
	}

	public Object getPony() {
		return pony;
	}
}
