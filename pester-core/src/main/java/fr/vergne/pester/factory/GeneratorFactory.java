package fr.vergne.pester.factory;

import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Type;

public class GeneratorFactory {
	
	private final TypeFactory typeFactory;
	
	// Package-scope constructor
	GeneratorFactory(TypeFactory typeFactory) {
		this.typeFactory = typeFactory;
	}
	
	public Generator<Byte> ofBytes() {
		return () -> (byte) 0;
	}

	public Generator<Short> ofShorts() {
		return () -> (short) 0;
	}

	public Generator<Integer> ofIntegers() {
		return () -> 0;
	}

	public Generator<Long> ofLongs() {
		return () -> 0L;
	}

	public Generator<Float> ofFloats() {
		return () -> 0F;
	}

	public Generator<Double> ofDoubles() {
		return () -> 0D;
	}

	public Generator<Boolean> ofBooleans() {
		return () -> false;
	}

	public Generator<Character> ofCharacters() {
		return () -> 'a';
	}

	public Generator<String> ofStrings() {
		return () -> "test";
	}

	public Generator<Object> ofObjects() {
		Object object = new Object();
		return () -> object;
	}
	
	public <T> Generator<List<T>> ofLists(Class<T> itemClass) {
		return ofLists(typeFactory.from(itemClass));
	}
	
	public <T> Generator<List<T>> ofLists(Type<T> itemType) {
		Generator<T> item = iteratorOf(itemType);
		List<T> list = IntStream.range(0, 3)
				.mapToObj(i -> item.create())
				.collect(Collectors.toList());
		return () -> list;
	}

	public <T> Generator<Set<T>> ofSets(Class<T> itemClass) {
		return ofSets(typeFactory.from(itemClass));
	}

	public <T> Generator<Set<T>> ofSets(Type<T> itemType) {
		LinkedHashSet<T> set = new LinkedHashSet<>(ofLists(itemType).create());
		return () -> set;
	}
	
	public <K, V> Generator<Map<K, V>> ofMaps(Class<K> keyClass, Class<V> valueClass) {
		return ofMaps(typeFactory.from(keyClass), typeFactory.from(valueClass));
	}
	
	public <K, V> Generator<Map<K, V>> ofMaps(Type<K> keyType, Type<V> valueType) {
		Generator<K> key = iteratorOf(keyType);
		Generator<V> value = iteratorOf(valueType);
		Map<K, V> map = IntStream.range(0, 3)
				.mapToObj(i -> i)
				.collect(Collectors.toMap(i -> key.create(), i -> value.create()));
		return () -> map;
	}

	public <T> Generator<T> iteratorOf(Type<T> type) {
		return new Generator<T>() {
			T value;
			
			@Override
			public T create() {
				if (value == null) {
					value = type.getGenerator().create();
				} else {
					value = type.getModifier().modify(value);
				}
				return value;
			}
		};
	}
	
	<T, A> Generator<A> ofArray(Class<A> arrayClass, Class<T> itemClass) {
		return () -> {
			int length = 3;
			Object newArray = Array.newInstance(itemClass, length);
			Generator<T> itemIterator = iteratorOf(typeFactory.from(itemClass));
			for (int i = 0; i < length; i++) {
				Array.set(newArray, i, itemIterator.create());
			}
			return arrayClass.cast(newArray);
		};
	}
}
