package fr.vergne.pester.model;

import java.util.Optional;
import java.util.function.Predicate;

import fr.vergne.pester.util.namer.NameNamer;
import fr.vergne.pester.util.namer.Namer;
import fr.vergne.pester.util.namer.PredicateNamer;

class TestNamer implements Namer {

	private Namer delegate;

	public TestNamer(String itemName) {
		this.delegate = new NameNamer(itemName) {
			@Override
			public String toString() {
				return "Namer[" + itemName + "]";
			}
		};
	}

	public TestNamer(String defaultName, Predicate<String> namePredicate) {
		this.delegate = new PredicateNamer(defaultName, namePredicate) {
			@Override
			public String toString() {
				return "Namer[PREDICATE]";
			}
		};
	}

	@Override
	public Optional<String> getExpectedName() {
		return delegate.getExpectedName();
	}

	@Override
	public Predicate<String> getNamePredicate() {
		return delegate.getNamePredicate();
	}

	@Override
	public String getDefaultName() {
		return delegate.getDefaultName();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
