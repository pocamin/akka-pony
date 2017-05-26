package org.pocamin.akka.pony.character;

import org.pocamin.akka.pony.FutureReturn;

/**

 */
public class TwilightSparkle {

	private final PinkiePie pinkiePie;

	public TwilightSparkle(PinkiePie pinkiePie) {
		this.pinkiePie = pinkiePie;
	}

	public FutureReturn<Void> pingPinkiePie() {
		return pinkiePie.updateState();
	}

}
