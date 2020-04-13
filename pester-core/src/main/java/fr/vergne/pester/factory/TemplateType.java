package fr.vergne.pester.factory;

import java.util.Optional;

import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Modifier;
import fr.vergne.pester.value.Type;

abstract class TemplateType<T> implements Type<T> {
	private Optional<Generator<T>> generator = Optional.empty();
	private Optional<Modifier<T>> modifier = Optional.empty();

	// TODO return optional?
	@Override
	public Generator<T> getGenerator() {
		return generator.orElse(() -> {
			throw new MissingGeneratorException(this);
		});
	}

	@Override
	public Type<T> withGenerator(Generator<T> generator) {
		this.generator = Optional.of(generator);
		return this;
	}

	// TODO return optional?
	@Override
	public Modifier<T> getModifier() {
		return modifier.orElse(x -> {
			throw new MissingModifierException(this);
		});
	}

	@Override
	public Type<T> withModifier(Modifier<T> modifier) {
		this.modifier = Optional.of(modifier);
		return this;
	}

	@SuppressWarnings("serial")
	private static class MissingGeneratorException extends IncompleteDefinitionException {
		public MissingGeneratorException(Type<?> type) {
			super("No generator registered for " + type);
		}
	}

	@SuppressWarnings("serial")
	private static class MissingModifierException extends IncompleteDefinitionException {
		public MissingModifierException(Type<?> type) {
			super("No modifier registered for " + type);
		}
	}
}
