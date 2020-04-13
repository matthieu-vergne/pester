package fr.vergne.pester.util.indexer;

public interface Indexer<T> {

	IndexedValue<T> decorateWithIndex(T value);

}