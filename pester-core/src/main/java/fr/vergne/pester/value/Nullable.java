package fr.vergne.pester.value;

public interface Nullable<T> {
	public T get();
	
	public static <T> Nullable<T> of(T value) {
		return () -> value;
	}
}
