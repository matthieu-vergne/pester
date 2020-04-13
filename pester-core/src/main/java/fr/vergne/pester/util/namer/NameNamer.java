package fr.vergne.pester.util.namer;

import java.util.Optional;
import java.util.function.Predicate;

import fr.vergne.pester.util.argscheck.ArgsCheck;

public class NameNamer implements Namer {
	private final String name;

	public NameNamer(String name) {
		this.name = ArgsCheck.requireNonNullNorEmpty(name, "No name provided");
	}

	@Override
	public Optional<String> getExpectedName() {
		return Optional.of(name);
	}

	@Override
	public Predicate<String> getNamePredicate() {
		return Predicate.isEqual(name);
	}

	@Override
	public String getDefaultName() {
		return name;
	}
}
