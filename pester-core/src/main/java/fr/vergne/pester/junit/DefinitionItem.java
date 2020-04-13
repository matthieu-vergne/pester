package fr.vergne.pester.junit;

import java.util.Optional;

import fr.vergne.pester.definition.ConstructorDefinition;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;

class DefinitionItem<P> {
	private final Optional<PojoDefinition<P>> pojo;
	private final Optional<ConstructorDefinition<P>> constructor;
	private final Optional<PropertyDefinition<P, ?>> property;
	private final Optional<Class<?>> parentClass;
	private final Optional<Class<?>> interfaceClass;

	public DefinitionItem(Optional<PojoDefinition<P>> pojo, Optional<ConstructorDefinition<P>> constructor,
			Optional<PropertyDefinition<P, ?>> property, Optional<Class<?>> parentClass,
			Optional<Class<?>> interfaceClass) {
		this.pojo = pojo;
		this.constructor = constructor;
		this.property = property;
		this.parentClass = parentClass;
		this.interfaceClass = interfaceClass;
	}

	public Optional<PojoDefinition<P>> getPojo() {
		return pojo;
	}

	public Optional<ConstructorDefinition<P>> getConstructor() {
		return constructor;
	}

	public Optional<PropertyDefinition<P, ?>> getProperty() {
		return property;
	}

	public Optional<Class<?>> getParentClass() {
		return parentClass;
	}

	public Optional<Class<?>> getInterfaceClass() {
		return interfaceClass;
	}
}
