package org.pocamin.akka.pony.performance;

import java.util.concurrent.CountDownLatch;

/**
 * (c) Swissquote 2017/05/26
 */
public class SynchronizedPinkiePie extends Thread {
	private final CountDownLatch latch;

	public SynchronizedPinkiePie(CountDownLatch latch) {
		this.latch = latch;
	}

	public synchronized void hello() {
		latch.countDown();
	}
}
