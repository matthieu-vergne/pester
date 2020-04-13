package fr.vergne.pester.model;

import java.util.Optional;

import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Option;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.argscheck.ArgsCheck;

public class Field<P> implements DefinitionSourcer {

	private final Class<P> pojoClass;
	private final String name;
	private final StackTraceElement[] definitionStackTrace;

	public Field(Class<P> pojoClass, String name) {
		this.pojoClass = ArgsCheck.requireNonNull(pojoClass, "No POJO class provided");
		this.name = ArgsCheck.requireNonNullNorEmpty(name, "No field name provided");
		
		this.definitionStackTrace = createDefinitionStackTraceFromHere();
	}
	
	@Override
	public StackTraceElement[] getDefinitionStackTrace() {
		return definitionStackTrace;
	}

	public boolean isPresent() {
		return searchField().isPresent();
	}

	public Class<?> getFieldClass() {
		return retrieveField().getType();
	}

	public Visibility getVisibility() {
		return Option.getFromModifiers(retrieveField().getModifiers(), Visibility.class);
	}

	public Mutability getMutability() {
		return Option.getFromModifiers(retrieveField().getModifiers(), Mutability.class);
	}

	public Scope getScope() {
		return Option.getFromModifiers(retrieveField().getModifiers(), Scope.class);
	}

	public void setTo(P pojo, Object value) {
		java.lang.reflect.Field field = retrieveField();
		field.setAccessible(true);
		try {
			field.set(pojo, value);
		} catch (IllegalArgumentException cause) {
			throw new DefinitionUnfulfilledException("Field " + name + " only accepts " + field.getType() + " values", cause);
		} catch (IllegalAccessException cause) {
			throw new ShouldNotOccurException(cause);
		}
	}

	public Object getFrom(P pojo) {
		java.lang.reflect.Field field = retrieveField();
		field.setAccessible(true);
		try {
			return field.get(pojo);
		} catch (IllegalAccessException cause) {
			throw new ShouldNotOccurException(cause);
		}
	}

	private java.lang.reflect.Field retrieveField() {
		return searchField().orElseThrow(() -> new DefinitionUnfulfilledException("Field " + name + " not found"));
	}
	
	private Optional<java.lang.reflect.Field> searchField() {
		try {
			return Optional.of(pojoClass.getDeclaredField(name));
		} catch (NoSuchFieldException cause) {
			return Optional.empty();
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
}
