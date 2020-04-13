package fr.vergne.pester.definition;

import static java.util.function.Predicate.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Option;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.namer.NameNamer;
import fr.vergne.pester.util.namer.Namer;
import fr.vergne.pester.util.namer.PredicateNamer;
import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Modifier;
import fr.vergne.pester.value.Nullable;
import fr.vergne.pester.value.Type;

public class PropertyDefinition<P, T> {

	private final Class<P> pojoClass;
	private final String name;
	private final Type<T> type;
	private Optional<FieldDefinition<P, T>> fieldDefinition = Optional.empty();
	private Optional<GetterDefinition<P, T>> getterDefinition = Optional.empty();
	private Optional<SetterDefinition<P, T>> setterDefinition = Optional.empty();
	private Optional<Modifier<T>> modifier = Optional.empty();
	private Optional<Generator<T>> generator = Optional.empty();
	private Optional<Nullable<T>> defaultValue = Optional.empty();
	
	public PropertyDefinition(Class<P> pojoClass, String name, Type<T> type) {
		this.pojoClass = pojoClass;
		this.name = name;
		this.type = type;
	}
	
	// BASE

	public String getName() {
		return name;
	}
	
	public Type<T> getType() {
		return type;
	}

	// FIELD
	
	public PropertyDefinition<P, T> withField(Option... options) {
		return withField(name, options);
	}

	public PropertyDefinition<P, T> withField(String name, Option... options) {
		return withFieldInternal(name, extractOption(options, Mutability.class), extractOption(options, Visibility.class), extractOption(options, Scope.class));
	}

	private static <T> Optional<T> extractOption(Option[] options, Class<T> optionType) {
		List<T> providedOptions = Stream.of(options)
				.distinct()
				.filter(optionType::isInstance)
				.map(optionType::cast)
				.collect(Collectors.toList());
		if (providedOptions.size() > 1) {
			throw new IllegalArgumentException(String.format("More than one %s provided: %s", optionType.getSimpleName(), providedOptions));
		} else {
			return providedOptions.stream().findFirst();
		}
	}

	private PropertyDefinition<P, T> withFieldInternal(String name, Optional<Mutability> mutability, Optional<Visibility> visibility, Optional<Scope> scope) {
		this.fieldDefinition = Optional.of(new FieldDefinition<>(pojoClass, name, visibility, mutability, scope));
		return this;
	}

	public Optional<FieldDefinition<P,T>> getFieldDefinition() {
		return fieldDefinition;
	}
	
	// GETTER

	private final Predicate<Object> isBooleanClass = isEqual(boolean.class).or(isEqual(Boolean.class));
	public PropertyDefinition<P, T> withGetter(Option... options) {
		List<String> possibleNames = new LinkedList<String>();
		possibleNames.add(name);
		possibleNames.add("get"+capitalize(name));
		if (type.getTypeClass().filter(isBooleanClass).isPresent()) {
			possibleNames.add("is" + capitalize(name));
		}
		
		Namer namer = new PredicateNamer("getter for " + name, createPatternForNames(possibleNames).asPredicate());
		return withGetter(namer, options);
	}
	
	public PropertyDefinition<P, T> withGetter(String name, Option... options) {
		return withGetter(new NameNamer(name), options);
	}

	private PropertyDefinition<P, T> withGetter(Namer namer, Option... options) {
		return withGetter(namer, extractOption(options, Mutability.class), extractOption(options, Visibility.class), extractOption(options, Scope.class));
	}

	private PropertyDefinition<P, T> withGetter(Namer namer, Optional<Mutability> mutability,
			Optional<Visibility> visibility, Optional<Scope> scope) {
		this.getterDefinition = Optional.of(new GetterDefinition<P, T>(pojoClass, namer, visibility, mutability, scope));
		return this;
	}

	public Optional<GetterDefinition<P, T>> getGetterDefinition() {
		return getterDefinition;
	}

	// SETTER
	
	public PropertyDefinition<P, T> withSetter(Option... options) {
		List<String> possibleNames = Arrays.asList(name, "set" + capitalize(name), "with" + capitalize(name));
		Namer namer = new PredicateNamer("setter for " + name, createPatternForNames(possibleNames).asPredicate());
		return withSetter(namer, options);
	}
	
	public PropertyDefinition<P, T> withSetter(String name, Option... options) {
		return withSetter(new NameNamer(name), options);
	}

	private PropertyDefinition<P, T> withSetter(Namer namer, Option... options) {
		return withSetter(namer, extractOption(options, Mutability.class), extractOption(options, Visibility.class), extractOption(options, Scope.class));
	}

	private PropertyDefinition<P, T> withSetter(Namer namer, Optional<Mutability> mutability,
			Optional<Visibility> visibility, Optional<Scope> scope) {
		this.setterDefinition = Optional.of(new SetterDefinition<>(pojoClass, type, namer, visibility, mutability, scope));
		return this;
	}

	public Optional<SetterDefinition<P, T>> getSetterDefinition() {
		return setterDefinition;
	}
	
	// DEFAULT VALUE
	
	public PropertyDefinition<P,T> withDefaultValue(T value) {
		this.defaultValue = Optional.of(Nullable.of(value));
		return this;
	}
	
	public Optional<Nullable<T>> getDefaultValue() {
		return defaultValue;
	}
	
	// GENERATOR
	
	public PropertyDefinition<P,T> withGenerator(Generator<T> generator) {
		this.generator = Optional.of(generator);
		return this;
	}

	public Generator<T> getGenerator() {
		return generator.orElse(type.getGenerator());
	}
	
	// MODIFIER
	
	public PropertyDefinition<P,T> withModifier(Modifier<T> modifier) {
		this.modifier = Optional.of(modifier);
		return this;
	}

	public Modifier<T> getModifier() {
		return modifier.orElse(type.getModifier());
	}

	// MISCELLANEOUS

	private String capitalize(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private Pattern createPatternForNames(List<String> names) {
		return Pattern.compile(
				names.stream()
				.map(Pattern::quote)
				.collect(Collectors.joining("|", "^", "$")));
	}
}
