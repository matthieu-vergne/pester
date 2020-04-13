package fr.vergne.pester.util.cache;

import java.util.Arrays;
import java.util.function.Function;

public class ParameteredKey<R> implements Key<R> {
	private final Object[] parameters;
	private final Function<Object, R> caster;

	@SuppressWarnings("unchecked")
	private ParameteredKey(Object[] parameters) {
		this(x -> (R) x, parameters);
	}

	private ParameteredKey(Class<R> type, Object[] parameters) {
		this(type::cast, parameters);
	}

	private ParameteredKey(Function<Object, R> caster, Object[] parameters) {
		this.parameters = parameters;
		this.caster = caster;
	}

	@Override
	public R cast(Object value) {
		return caster.apply(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ParameteredKey) {
			ParameteredKey<?> that = (ParameteredKey<?>) obj;
			return Arrays.deepEquals(this.parameters, that.parameters);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(parameters);
	}

	@Override
	public String toString() {
		return Arrays.deepToString(parameters);
	}

	public static <T> ParameteredKey<T> create(Object... parameters) {
		return new ParameteredKey<T>(parameters);
	}

	public static <T> ParameteredKey<T> createSafe(Class<T> type, Object... parameters) {
		return new ParameteredKey<T>(type, parameters);
	}
}
