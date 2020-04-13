package fr.vergne.pester.util.indexer.impl;

import fr.vergne.pester.util.indexer.IndexedValue;
import fr.vergne.pester.util.indexer.Indexer;

public class IteratorIndexer<T> implements Indexer<T> {

	private int nextIndex = 0;

	@Override
	public IndexedValue<T> decorateWithIndex(T value) {
		return new RamIndexedValue<T>(value, nextIndex++);
	}
}
