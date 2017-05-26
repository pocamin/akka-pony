package org.pocamin.akka.pony;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import akka.actor.ActorRef;

/**
 * (c) Swissquote 2017/05/26
 */
class FutureReturnImpl<K> extends CompletableFuture<FutureReturn.Result<K>> implements FutureReturn<K> {

	public FutureReturnImpl() {

	}

	public FutureReturnImpl(K value) {
		this.complete(new Result<>(value));
	}

	void completeOnError(Throwable error) {
		this.complete(new Result<K>(error));
	}

	@Override
	public void forward(ActorRef actorRef) {
		thenAccept(v -> actorRef.tell(v.getValue(), ActorRef.noSender()));
	}

	@Override
	public <U> void forwardError(ActorRef actorRef) {
		thenAccept(v -> actorRef.tell(v.getError(), ActorRef.noSender()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> FutureReturn<U> forward(Function<K, FutureReturn<U>> forwardMethodCall) {
		FutureReturnImpl<U> toReturn = new FutureReturnImpl<>();
		thenAccept(v -> {
			if (v.getError() != null) {
				toReturn.completeOnError(v.getError());
			} else {
				((FutureReturnImpl<U>) forwardMethodCall.apply(v.getValue()))
						.thenApply(toReturn::complete);
			}
		});
		return toReturn;
	}

	@Override
	public <U> FutureReturn<U> forward(BiFunction<K, Throwable, FutureReturn<U>> forwardMethodCall) {
		FutureReturnImpl<U> toReturn = new FutureReturnImpl<>();
		thenAccept(v -> ((FutureReturnImpl<U>) forwardMethodCall.apply(v.getValue(), v.getError())).thenApply(toReturn::complete));
		return toReturn;
	}

	void completeOther(FutureReturnImpl<K> other) {
		thenAccept(other::complete);
	}

}
