package fr.vergne.pester.util.namer;

import java.util.Optional;
import java.util.function.Predicate;

public interface Namer {

	Optional<String> getExpectedName();

	Predicate<String> getNamePredicate();

	String getDefaultName();

}