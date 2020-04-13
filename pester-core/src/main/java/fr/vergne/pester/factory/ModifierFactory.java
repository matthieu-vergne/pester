package fr.vergne.pester.factory;

import static fr.vergne.pester.factory.RestartCondition.*;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import fr.vergne.pester.value.Modifier;
import fr.vergne.pester.value.Type;

public class ModifierFactory {
	
	private final GeneratorFactory generator;
	private final TypeFactory typeFactory;
	
	// Package-scope constructor
	ModifierFactory(TypeFactory typeFactory, GeneratorFactory generatorFactory) {
		this.typeFactory = typeFactory;
		this.generator = generatorFactory;
	}

	public Modifier<Byte> ofBytes() {
		return of((byte) 0, b -> ++b, oneDigitRange());
	}

	public Modifier<Short> ofShorts() {
		return of((short) 0, s -> ++s, oneDigitRange());
	}

	public Modifier<Integer> ofIntegers() {
		return of(0, i -> i + 1, oneDigitRange());
	}

	public Modifier<Long> ofLongs() {
		return of(0L, l -> l + 1, oneDigitRange());
	}

	public Modifier<Float> ofFloats() {
		return of(0F, f -> f + 0.1F, oneDigitRange());
	}

	public Modifier<Double> ofDoubles() {
		return of(0D, d -> d + 0.1, oneDigitRange());
	}

	public Modifier<Boolean> ofBooleans() {
		return of(false, b -> !b);
	}

	public Modifier<Character> ofCharacters() {
		// Keep it in a-z to look like a character
		return of('a', c -> ++c, range('a', 'z'));
	}

	public Modifier<String> ofStrings() {
		// Use an upper bound of 10 chars to keep a reasonable memory footprint
		return of("test", s -> s + "*", s -> s.length() >= 10);
	}

	public Modifier<Object> ofObjects() {
		return of(new Object(), o -> new Object());
	}

	public <T> Modifier<List<T>> ofLists(Class<T> itemClass) {
		return ofLists(typeFactory.from(itemClass));
	}

	public <T> Modifier<List<T>> ofLists(Type<T> itemType) {
		List<T> start = generator.ofLists(itemType).create();
		Modifier<T> itemModifier = itemType.getModifier();
		return of(
				start,
				list -> list.stream()
						.map(itemModifier::modify)
						.collect(Collectors.toList()));
	}

	public <T> Modifier<Set<T>> ofSets(Class<T> itemClass) {
		return ofSets(typeFactory.from(itemClass));
	}

	public <T> Modifier<Set<T>> ofSets(Type<T> itemType) {
		Set<T> start = generator.ofSets(itemType).create();
		Modifier<T> itemModifier = itemType.getModifier();
		return of(
				start,
				set -> set.stream()
						.map(itemModifier::modify)
						.collect(Collectors.toSet()));
	}

	public <K, V> Modifier<Map<K, V>> ofMaps(Class<K> keyClass, Class<V> valueClass) {
		return ofMaps(typeFactory.from(keyClass), typeFactory.from(valueClass));
	}

	public <K, V> Modifier<Map<K, V>> ofMaps(Type<K> keyType, Type<V> valueType) {
		Map<K, V> start = generator.ofMaps(keyType, valueType).create();
		Modifier<K> keyModifier = keyType.getModifier();
		Modifier<V> valueModifier = valueType.getModifier();
		return of(
				start,
				map -> map.entrySet().stream()
						.collect(Collectors.toMap(
								entry -> keyModifier.modify(entry.getKey()),
								entry -> valueModifier.modify(entry.getValue()))));
	}

	public <T> Modifier<T> of(T start, UnaryOperator<T> updater) {
		return of(start, updater, noRestart());
	}
	
	public <T> Modifier<T> of(T start, UnaryOperator<T> updater, RestartCondition<T> restartCondition) {
		return x -> x == null || restartCondition.test(x) ? start : updater.apply(x);
	}
	
	@SuppressWarnings("unchecked")
	<A, T> Modifier<A> ofArray(Class<A> arrayClass, Class<T> itemClass) {
		return oldArray -> {
			if (oldArray == null) {
				return typeFactory.from(arrayClass).getGenerator().create();
			} else {
				Modifier<T> itemModifier = typeFactory.from(itemClass).getModifier();
				int length = Array.getLength(oldArray);
				Object newArray = Array.newInstance(itemClass, length);
				for (int i = 0; i < length; i++) {
					T oldItem = (T) Array.get(oldArray, i);
					T newItem = itemModifier.modify(oldItem);
					Array.set(newArray, i, newItem);
				}
				return arrayClass.cast(newArray);
			}
		};
	}
}
