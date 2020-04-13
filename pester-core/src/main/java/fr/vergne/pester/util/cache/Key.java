package fr.vergne.pester.util.cache;

public interface Key<R> {

	R cast(Object value);

}