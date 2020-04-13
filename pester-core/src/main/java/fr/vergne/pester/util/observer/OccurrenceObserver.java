package fr.vergne.pester.util.observer;

public class OccurrenceObserver<T> {
	private T value;

	public void occurs(T value) {
		if (this.value == null) {
			this.value = value;
		}
	}

	public boolean hasOccurred() {
		return value != null;
	}

	public T getFirstOccurrence() {
		return value;
	}
}
