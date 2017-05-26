package org.pocamin.akka.pony;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pocamin.akka.pony.character.Rarity;

import org.pocamin.akka.pony.character.Applejack;
import org.pocamin.akka.pony.character.Fluttershy;

import akka.actor.ActorSystem;

/**
 * 2017/05/24
 */
public class PonyTest {
	private Rarity rarity;
	private Applejack applejack;
	private Fluttershy fluttershy;

	@Before
	public void initializeService() {
		ActorSystem actorSystem = ActorSystem.apply("test");
		// no arguments
		fluttershy = Pony.newPony(Fluttershy.class, actorSystem);
		// with arguments
		rarity = Pony.newPony(Rarity.class, actorSystem, fluttershy);
		applejack = Pony.newPony(Applejack.class, actorSystem, rarity, fluttershy);
	}

	@Test(expected = PonyAlreadyKilledException.class)
	public void testKillActorSafe() throws Throwable {
		Pony.kill(fluttershy);
		throw fluttershy.decorateString("killing is not magic").get().getError();
	}

	@Test(expected = IllegalAccessError.class)
	public void cannotKillANotPony() throws Throwable {
		Pony.kill("test");
	}

	@Test
	public void testKillActorUnsafe() throws Throwable {
		Pony.kill(fluttershy, Safety.UNSAFE);
		Thread t = new Thread(() -> {
			try {
				fluttershy.takeANap();
			}
			catch (InterruptedException e) {//
			}
		}, "test");
		t.start();
		Thread.sleep(100);
		Assert.assertTrue(t.isAlive());
	}


	@Test
	public void testPonyUseActor() throws Throwable {
		Thread t = new Thread(() -> {
			try {
				fluttershy.takeANap();
			}
			catch (InterruptedException e) {//
			}
		}, "test");
		t.start();
		Thread.sleep(100);
		Assert.assertFalse(t.isAlive());
	}



	@Test
	public void testSimpleCall() throws InterruptedException, ExecutionException, TimeoutException {
		FutureReturn.Result<String> result = applejack.decorateString("Actor Is Magic").get(2, SECONDS);
		Assert.assertEquals("Applejack says > Fluttershy says > Rarity says > Actor Is Magic", result.getValue());
		Assert.assertNull(result.getError());
	}

	@Test
	public void testSimpleCallWithError() throws InterruptedException, ExecutionException, TimeoutException {
		Assert.assertEquals("No thanks", applejack.useRoundup().get(2, SECONDS).getError().getMessage());
	}

	@Test
	public void testSimpleCallWithForwardError() throws InterruptedException, ExecutionException, TimeoutException {
		Assert.assertEquals("Fluttershy says > Applejack says > No thanks",
				applejack.useRoundup()
						.forward((value, error) -> applejack.selfDecorate(error.getMessage()))
						.forward(value -> fluttershy.decorateString(value))
				.get(2, SECONDS).getValue());
	}

	@Test
	public void testSimpleCallWithErrorChain() throws InterruptedException, ExecutionException, TimeoutException {
		AtomicInteger value = new AtomicInteger();
		FutureReturn.Result<Integer> result =
				applejack.useRoundup()
						.forward(__ -> rarity.incrementAndGet(value))
						.get(2, SECONDS);
		Assert.assertEquals("No thanks", result.getError().getMessage());
		Assert.assertEquals(0, value.get());

		FutureReturn.Result<Void> resultReverse =
				rarity.incrementAndGet(value)
						.forward(__ -> applejack.useRoundup())
						.get(2, SECONDS);
		Assert.assertEquals("No thanks", resultReverse.getError().getMessage());
		Assert.assertEquals(1, value.get());
	}

}