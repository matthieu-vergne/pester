package fr.vergne.pester.factory;

import java.util.Optional;

import fr.vergne.pester.value.Type;

class ClassType<T> extends TemplateType<T> implements Type<T> {

	private final Class<T> typeClass;

	public ClassType(Class<T> typeClass) {
		this.typeClass = typeClass;
	}

	@Override
	public Optional<Class<?>> getTypeClass() {
		return Optional.of(typeClass);
	}

	public Class<?> requireTypeClass() {
		return typeClass;
	}

	@Override
	public String getName() {
		return typeClass.getName();
	}

	@Override
	public String toString() {
		return typeClass.toString();
	}
}
