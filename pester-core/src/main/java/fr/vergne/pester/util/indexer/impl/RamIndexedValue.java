package fr.vergne.pester.util.indexer.impl;

import fr.vergne.pester.util.indexer.IndexedValue;

public class RamIndexedValue<T> implements IndexedValue<T> {
	private final T value;
	private final int index;

	RamIndexedValue(T value, int index) {
		this.value = value;
		this.index = index;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public int getIndex() {
		return index;
	}
}
