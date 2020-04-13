package fr.vergne.pester.value;

import java.util.Optional;

public interface Type<T> {
	Optional<Class<?>> getTypeClass();

	String getName();

	Type<T> withGenerator(Generator<T> generator);

	Generator<T> getGenerator();

	Type<T> withModifier(Modifier<T> modifier);

	Modifier<T> getModifier();
}