package org.pocamin.akka.pony.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pocamin.akka.pony.character.PinkiePie;

import org.pocamin.akka.pony.Pony;
import org.pocamin.akka.pony.character.TwilightSparkle;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**

 */
@Ignore
public class PerformanceTest {

	private ActorSystem actorSystem;

	public static final int nbTwilightSparkle = 1000;
	private int nbMessages = 1000000;
	private CountDownLatch latch = new CountDownLatch(nbMessages);

	@Before
	public void initializeService() {
		this.actorSystem = ActorSystem.apply("test");

	}

	@Test
	public void testPerf() throws InterruptedException {
		PinkiePie pinkiePie = Pony.newPony(PinkiePie.class, actorSystem, latch);
		TwilightSparkle[] twilightSparkles = new TwilightSparkle[nbTwilightSparkle];
		for (int i = 0; i < nbTwilightSparkle; i++) {
			twilightSparkles[i] = Pony.newPony(TwilightSparkle.class, actorSystem, pinkiePie);
		}
		long tm = System.currentTimeMillis();
		for (int i = 0; i < nbMessages; i++) {
			twilightSparkles[i % nbTwilightSparkle].pingPinkiePie();
		}

		latch.await();

		System.out.println("Pony took "  + (System.currentTimeMillis() - tm) + " ms" );
	}

	@Test
	public void testPerfExecutor() throws InterruptedException {
		SynchronizedPinkiePie pinkiePie = new SynchronizedPinkiePie(latch);
		SynchronizedTwilightSparkle[] twilightSparkles = new SynchronizedTwilightSparkle[nbTwilightSparkle];

		for (int i = 0; i < nbTwilightSparkle; i++) {
			twilightSparkles[i] = new SynchronizedTwilightSparkle(pinkiePie);
		}
		Executor threadPoolExecutor = Executors.newWorkStealingPool(5);
		long tm = System.currentTimeMillis();
		for (int i = 0; i < nbMessages; i++) {
			int finalI = i;
			threadPoolExecutor.execute(() -> twilightSparkles[finalI % nbTwilightSparkle].proceed());

		}

		latch.await();

		System.out.println("Executor took "  + (System.currentTimeMillis() - tm) + " ms" );


	}

	@Test
	public void testPerfAkka() throws InterruptedException {
		ActorRef pinkiePie = actorSystem.actorOf(Props.create(PinkiePieActor.class, () -> new PinkiePieActor(latch)));
		ActorRef[] twilightSparkles = new ActorRef[nbTwilightSparkle];
		for (int i = 0; i < nbTwilightSparkle; i++) {
			twilightSparkles[i] = actorSystem.actorOf(Props.create(TwilightSparkleActor.class, () -> new TwilightSparkleActor(pinkiePie)));;
		}
		long tm = System.currentTimeMillis();
		for (int i = 0; i < nbMessages; i++) {
			twilightSparkles[i % nbTwilightSparkle].tell("ping", ActorRef.noSender());
		}

		latch.await();

		System.out.println("Akka took "  + (System.currentTimeMillis() - tm) + " ms" );
	}



}
