package fr.vergne.pester.definition;

import java.util.Optional;

import fr.vergne.pester.model.Field;
import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;

public class FieldDefinition<P, T> implements InstanciableDefinition<Field<P>> {

	private final Field<P> instance;
	private final Optional<Visibility> visibility;
	private final Optional<Mutability> mutability;
	private final Optional<Scope> scope;

	public FieldDefinition(Class<P> pojoClass, String name, Optional<Visibility> visibility,
			Optional<Mutability> mutability, Optional<Scope> scope) {
		this.instance = new Field<>(pojoClass, name);
		this.visibility = visibility;
		this.mutability = mutability;
		this.scope = scope;
	}

	@Override
	public Field<P> getInstance() {
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
