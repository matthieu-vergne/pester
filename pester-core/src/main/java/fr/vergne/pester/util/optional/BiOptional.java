package fr.vergne.pester.util.optional;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BiOptional<T, U extends T, V extends T> {

	private final Optional<U> u;
	private final Optional<V> v;

	BiOptional(Optional<U> u, Optional<V> v) {
		this.u = u;
		this.v = v;
	}

	public static <T, U extends T, V extends T> BiOptional<T, U, V> ofNullables(U u, V v) {
		return new BiOptional<>(Optional.ofNullable(u), Optional.ofNullable(v));
	}

	public <R> BiOptional<R, R, R> mapEach(Function<T, R> mapper) {
		return new BiOptional<>(u.map(mapper), v.map(mapper));
	}

	public <R> Optional<R> mapBoth(BiFunction<U, V, R> mapper, Function<U, R> mapperFirst,
			Function<V, R> mapperSecond) {
		if (u.isPresent() && v.isPresent()) {
			return Optional.ofNullable(mapper.apply(u.get(), v.get()));
		} else if (u.isPresent()) {
			return u.map(mapperFirst);
		} else if (v.isPresent()) {
			return v.map(mapperSecond);
		} else {
			return Optional.empty();
		}
	}

}
