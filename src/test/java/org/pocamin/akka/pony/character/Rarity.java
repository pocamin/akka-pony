package org.pocamin.akka.pony.character;

import static org.pocamin.akka.pony.FutureReturn.shouldReturn;

import java.util.concurrent.atomic.AtomicInteger;

import org.pocamin.akka.pony.FutureReturn;

/**

 */
public class Rarity {

	private final Fluttershy fluttershy;

	public Rarity(Fluttershy fluttershy) {
		this.fluttershy = fluttershy;
	}

	FutureReturn<String> decorateString(String toDecorate) {
		return shouldReturn("Rarity says > " + toDecorate);
	}

	public FutureReturn<Integer> incrementAndGet(AtomicInteger value) {
		return shouldReturn(value.incrementAndGet());
	}
}
