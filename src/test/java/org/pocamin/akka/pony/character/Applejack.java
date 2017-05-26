package org.pocamin.akka.pony.character;

import org.pocamin.akka.pony.FutureReturn;

/**

 */
public class Applejack {
	private final Rarity rarity;
	private final Fluttershy fluttershy;

	public Applejack(Rarity rarity, Fluttershy servicec) {
		this.rarity = rarity;
		this.fluttershy = servicec;
	}

	public FutureReturn<String> decorateString(String toDecorate) {
		return rarity.decorateString(toDecorate)
				.forward(fluttershy::decorateString)
				.forward(this::selfDecorate);
	}

	public FutureReturn<String> selfDecorate(String toDecorate) {
		return FutureReturn.shouldReturn("Applejack says > " + toDecorate);
	}

	public FutureReturn<Void> useRoundup() {
		throw new IllegalAccessError("No thanks");
	}
}
