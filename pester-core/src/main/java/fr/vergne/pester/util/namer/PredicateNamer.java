package fr.vergne.pester.util.namer;

import java.util.Optional;
import java.util.function.Predicate;

import fr.vergne.pester.util.argscheck.ArgsCheck;

public class PredicateNamer implements Namer {
	private final String defaultName;
	private final Predicate<String> namePredicate;

	public PredicateNamer(String defaultName, Predicate<String> namePredicate) {
		this.defaultName = ArgsCheck.requireNonNull(defaultName, "No default name provided");
		this.namePredicate = ArgsCheck.requireNonNull(namePredicate, "No name predicate provided");
	}

	@Override
	public Optional<String> getExpectedName() {
		return Optional.empty();
	}

	@Override
	public Predicate<String> getNamePredicate() {
		return namePredicate;
	}

	@Override
	public String getDefaultName() {
		return defaultName;
	}
}
