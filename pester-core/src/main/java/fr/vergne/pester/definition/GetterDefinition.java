package fr.vergne.pester.definition;

import java.util.Optional;

import fr.vergne.pester.model.Getter;
import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.namer.Namer;

public class GetterDefinition<P, T> implements InstanciableDefinition<Getter<P>> {

	private final Getter<P> instance;
	private final Optional<Visibility> visibility;
	private final Optional<Mutability> mutability;
	private final Optional<Scope> scope;

	public GetterDefinition(Class<P> pojoClass, Namer namer, Optional<Visibility> visibility,
			Optional<Mutability> mutability, Optional<Scope> scope) {
		this.instance = new Getter<>(pojoClass, namer);
		this.visibility = visibility;
		this.mutability = mutability;
		this.scope = scope;
	}

	@Override
	public Getter<P> getInstance() {
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
