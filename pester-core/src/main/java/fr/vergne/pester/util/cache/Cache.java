package fr.vergne.pester.util.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Cache {

	<T> T get(Key<T> key);

	<T> T get(Key<T> key, Supplier<T> supplier);

	public static Cache create() {
		Map<Key<?>, Object> map = new HashMap<>();
		return new Cache() {
			
			@Override
			public <T> T get(Key<T> key) {
				return get(key, () -> {throw new IllegalStateException("No value stored for " + key);});
			}
			
			@Override
			public <T> T get(Key<T> key, Supplier<T> supplier) {
				return key.cast(map.computeIfAbsent(key, k -> supplier.get()));
			}
		};
	}

	public static <T, R> Function<T, R> onFunction(Function<T, R> function) {
		Cache cache = create();
		return value -> cache.get(ParameteredKey.create(value), () -> function.apply(value));
	}

	public static <R> Supplier<R> onSupplier(Supplier<R> supplier) {
		Cache cache = create();
		Key<R> key = ParameteredKey.create(new Object());
		return () -> cache.get(key, supplier);
	}
}