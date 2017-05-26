package org.pocamin.akka.pony.character;

import org.pocamin.akka.pony.FutureReturn;

/**

 */
public class Fluttershy {
	public FutureReturn<String> decorateString(String toDecorate) {
		return FutureReturn.shouldReturn("Fluttershy says > " + toDecorate);
	}

	public FutureReturn<Boolean> takeANap() throws InterruptedException {
		Thread.sleep(60000);
		return FutureReturn.TRUE;

	}

}
