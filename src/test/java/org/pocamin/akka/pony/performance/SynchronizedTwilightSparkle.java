package org.pocamin.akka.pony.performance;

/**
 * (c) Swissquote 2017/05/26
 */
public class SynchronizedTwilightSparkle {
	private final SynchronizedPinkiePie synchronizedPinkiePie;

	SynchronizedTwilightSparkle(SynchronizedPinkiePie synchronizedPinkiePie) {
		this.synchronizedPinkiePie = synchronizedPinkiePie;
	}

	synchronized void proceed() {
		synchronizedPinkiePie.hello();

	}

}
