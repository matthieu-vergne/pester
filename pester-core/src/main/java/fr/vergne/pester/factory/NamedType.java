package fr.vergne.pester.factory;

import java.util.Optional;

class NamedType<T> extends TemplateType<T> {
	
	private final String name;
	private final Optional<Class<?>> typeClass;

	public NamedType(String name, Optional<Class<?>> typeClass) {
		this.name = name;
		this.typeClass = typeClass;
	}

	@Override
	public Optional<Class<?>> getTypeClass() {
		return typeClass;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
