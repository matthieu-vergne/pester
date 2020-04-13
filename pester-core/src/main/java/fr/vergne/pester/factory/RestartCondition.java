package fr.vergne.pester.factory;

import java.util.function.Predicate;

interface RestartCondition<T> extends Predicate<T> {
	public static <T> RestartCondition<T> noRestart() {
		return x -> false;
	}

	public static <T extends Comparable<T>> RestartCondition<T> range(T minInclusive, T maxExclusive) {
		return x -> x.compareTo(minInclusive) < 0 || x.compareTo(maxExclusive) >= 0;
	}

	public static <T extends Number> RestartCondition<T> oneDigitRange() {
		RestartCondition<Byte> range = range((byte) 0, (byte) 10);
		return n -> range.test(n.byteValue());
	}
}
