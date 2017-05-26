package org.pocamin.akka.pony;

import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;

import akka.actor.ActorRef;

/**
 */
public interface FutureReturn<K> extends Future<FutureReturn.Result<K>> {

	FutureReturn<Void> VOID = shouldReturn(null);
	FutureReturn<Boolean> TRUE = shouldReturn(Boolean.TRUE);
	FutureReturn<Boolean> FALSE = shouldReturn(Boolean.FALSE);

	void forward(ActorRef actorRef);

	<U> void forwardError(ActorRef actorRef);

	@SuppressWarnings("unchecked")
	<U> FutureReturn<U> forward(Function<K, FutureReturn<U>> forwardMethodCall);

	<U>  FutureReturn<U> forward(BiFunction<K, Throwable, FutureReturn<U>> forwardMethodCall);

	static <K> FutureReturn<K> shouldReturn(K value) {
		return new FutureReturnImpl<>(value);
	}

	static <K> FutureReturn<K> error(Throwable error) {
		FutureReturnImpl<K> wrappedError = new FutureReturnImpl<>();
		wrappedError.completeOnError(error);
		return wrappedError;
	}

	class Result<K> {
		private final K value;
		private final Throwable error;

		Result(K value) {
			this.error = null;
			this.value = value;
		}

		Result(Throwable error) {
			this.value = null;
			this.error = error;
		}

		public K getValue() {
			return value;
		}

		public Throwable getError() {
			return error;
		}
	}
}
