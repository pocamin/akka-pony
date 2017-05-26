package org.pocamin.akka.pony.character;

import java.util.concurrent.CountDownLatch;

import org.pocamin.akka.pony.FutureReturn;

/**

 */
public class PinkiePie {

	private final CountDownLatch latch;

	public PinkiePie(CountDownLatch latch) {
		this.latch = latch;
	}

	public FutureReturn<Void> updateState() {
		latch.countDown();
		return FutureReturn.VOID;
	}

}
