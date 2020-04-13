package fr.vergne.pester.definition;

import java.util.Optional;

import fr.vergne.pester.model.Setter;
import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.namer.Namer;
import fr.vergne.pester.value.Type;

public class SetterDefinition<P, T> implements InstanciableDefinition<Setter<P>> {

	private final Setter<P> instance;
	private final Optional<Visibility> visibility;
	private final Optional<Mutability> mutability;
	private final Optional<Scope> scope;

	public SetterDefinition(Class<P> pojoClass, Type<T> parameterType, Namer namer, Optional<Visibility> visibility,
			Optional<Mutability> mutability, Optional<Scope> scope) {
		this.instance = new Setter<>(pojoClass, parameterType, namer);
		this.visibility = visibility;
		this.mutability = mutability;
		this.scope = scope;
	}

	@Override
	public Setter<P> getInstance() {
		return instance;
	}

	public Optional<Visibility> getVisibility() {
		return visibility;
	}

	public Optional<Mutability> getMutability() {
		return mutability;
	}

	public Optional<Scope> getScope() {
		return scope;
	}
}
