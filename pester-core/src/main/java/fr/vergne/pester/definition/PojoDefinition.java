package fr.vergne.pester.definition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.vergne.pester.factory.Factory;
import fr.vergne.pester.junit.annotation.ConstructorPropertiesHelper;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.cache.Cache;
import fr.vergne.pester.util.cache.Key;
import fr.vergne.pester.util.cache.ParameteredKey;
import fr.vergne.pester.util.observer.OccurrenceObserver;
import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Type;

public class PojoDefinition<P> {
	
	private final Factory factory;
	private final Class<P> pojoClass;
	private Optional<Class<?>> parentClass = Optional.empty();
	private final Collection<Class<?>> interfaces = new LinkedHashSet<>();
	private final Optional<Generator<P>> pojoGenerator;
	private final Map<String, PropertyDefinition<P, ?>> properties;
	private final Map<List<Type<?>>, ConstructorDefinition<P>> constructors;

	/** Used only if no generator is set and no valid constructor is found */
	private final NullPointerException nullPojoGeneratorException;
	
	private final Cache cache = Cache.create();
	
	public PojoDefinition(Class<P> pojoClass) {
		this(pojoClass, new Factory());
	}

	public PojoDefinition(Class<P> pojoClass, Factory factory) {
		this(pojoClass, Optional.empty(), factory);
	}

	public PojoDefinition(Generator<P> pojoGenerator) {
		this(pojoGenerator, new Factory());
	}

	@SuppressWarnings("unchecked")
	public PojoDefinition(Generator<P> pojoGenerator, Factory factory) {
		this((Class<P>) pojoGenerator.create().getClass(), Optional.of(pojoGenerator), factory);
	}

	public PojoDefinition(Class<P> pojoClass, Generator<P> pojoGenerator) {
		this(pojoClass, Optional.of(pojoGenerator), new Factory());
	}

	private PojoDefinition(Class<P> pojoClass, Optional<Generator<P>> optional, Factory factory) {
		this.pojoClass = pojoClass;
		this.properties = new LinkedHashMap<>();
		this.constructors = new LinkedHashMap<>();
		this.pojoGenerator = optional;
		this.nullPojoGeneratorException = new NullPointerException("No Pojo generator set, set one or add constructors");
		this.factory = factory;
	}
	
	public Factory create() {
		return factory;
	}
	
	// POJO
	
	public Class<P> getPojoClass() {
		return pojoClass;
	}

	public void setParentClass(Class<?> parentClass) {
		if (parentClass.isInterface()) {
			throw new InterfaceClassException(parentClass);
		} else {
			this.parentClass = Optional.of(parentClass);
		}
	}
	
	public Optional<Class<?>> getParentClass() {
		return parentClass;
	}

	public void addInterfaces(Class<?>... interfaceClasses) {
		Optional<Class<?>> nonInterfaceClass = Stream.of(interfaceClasses)
				.filter(interfaceClass -> !interfaceClass.isInterface())
				.findAny();
		
		if (nonInterfaceClass.isPresent()) {
			throw new NonInterfaceClassException(nonInterfaceClass.get());
		} else {
			interfaces.addAll(Arrays.asList(interfaceClasses));
		}
	}
	
	public Collection<Class<?>> getInterfaces() {
		return interfaces;
	}
	
	private final Key<Generator<P>> pojoGeneratorKey = ParameteredKey.create(new Object());
	public Generator<P> getPojoGenerator() {
		return pojoGenerator.orElseGet(() -> cache.get(pojoGeneratorKey, this::createPojoGeneratorFromConstructors));
	}

	private Generator<P> createPojoGeneratorFromConstructors() {
		return () -> {
			OccurrenceObserver<Exception> generatorException = new OccurrenceObserver<>();
			OccurrenceObserver<ConstructorCannotGeneratePojoException> invalidConstructorException = new OccurrenceObserver<>();
			Optional<P> pojo = getConstructors().stream()
					.sorted(Comparator.comparing(constructor -> constructor.getParametersDefinitions().size()))
					.map(definition -> {
						List<?> parameters;
						try {
							parameters = definition.getParametersGenerator().create();
						} catch (Exception cause) {
							generatorException.occurs(cause);
							return null;
						}
						fr.vergne.pester.model.Constructor<P> constructor = definition.getInstance();
						try {
							return constructor.invoke(parameters);
						} catch (Exception cause) {
							invalidConstructorException.occurs(new ConstructorCannotGeneratePojoException(cause));
							return null;
						}
					})
					.filter(Objects::nonNull)
					.findAny();
			
			if (pojo.isPresent()) {
				return pojo.get();
			} else if (generatorException.hasOccurred()) {
				throw new PojoGeneratorNotFoundException(generatorException.getFirstOccurrence());
			} else if (invalidConstructorException.hasOccurred()) {
				throw new PojoGeneratorNotFoundException(invalidConstructorException.getFirstOccurrence());
			} else {
				throw new PojoGeneratorNotFoundException(nullPojoGeneratorException);
			}
		};
	}
	
	// CONSTRUCTORS
	
	@SafeVarargs
	public final ConstructorDefinition<P> addConstructor(Visibility visibility, PropertyDefinition<P, ?>... parameters) {
		return addConstructor(Optional.of(visibility), parameters);
	}

	@SafeVarargs
	public final ConstructorDefinition<P> addConstructor(PropertyDefinition<P, ?>... parameters) {
		return addConstructor(Optional.empty(), parameters);
	}

	@SafeVarargs
	private final ConstructorDefinition<P> addConstructor(Optional<Visibility> visibility,
			PropertyDefinition<P, ?>... parameters) {
		List<PropertyDefinition<P, ?>> params = Arrays.asList(parameters);
		List<Type<?>> types = params.stream().map(PropertyDefinition::getType).collect(Collectors.toList());
		return addConstructor(visibility, params, types);
	}

	private ConstructorDefinition<P> addConstructor(Optional<Visibility> visibility, List<PropertyDefinition<P, ?>> params,
			List<Type<?>> types) {
		return (ConstructorDefinition<P>) constructors.compute(types, (k, previousDefinition) -> {
			if (previousDefinition == null) {
				return new ConstructorDefinition<>(pojoClass, params, visibility);
			} else {
				throw new IllegalArgumentException("Already defined constructor on " + types);
			}
		});
	}
	
	public Collection<ConstructorDefinition<P>> getConstructors() {
		return constructors.values();
	}

	// PROPERTIES
	
	public <T> PropertyDefinition<P, T> addProperty(Class<T> typeClass) {
		return addProperty(factory.type().from(typeClass));
	}

	public <T> PropertyDefinition<P, T> addProperty(Type<T> type) {
		return addProperty(type, Optional.empty());
	}

	<T> PropertyDefinition<P, T> addProperty(Class<T> typeClass, Optional<String> name) {
		return addProperty(factory.type().from(typeClass), name);
	}
	
	private long unnamedFieldIndex = 0;
	private final Supplier<String> autoFieldNamer = () -> "?"+(++unnamedFieldIndex);
	<T> PropertyDefinition<P, T> addProperty(Type<T> type, Optional<String> name) {
		return addProperty(type, name.orElseGet(autoFieldNamer));
	}
	
	public <T> PropertyDefinition<P, T> addProperty(Class<T> typeClass, String name) {
		return addProperty(factory.type().from(typeClass), name);
	}
	
	@SuppressWarnings("unchecked")
	public <T> PropertyDefinition<P, T> addProperty(Type<T> type, String name) {
		if (name.equals(ConstructorPropertiesHelper.NON_PROPERTY)) {
			return addProperty(type);
		} else {
			return (PropertyDefinition<P, T>) properties.compute(name, (k, previousDefinition) -> {
				if (previousDefinition == null) {
					return new PropertyDefinition<>(pojoClass, name, type);
				} else {
					throw new AlreadyDefinedPropertyException(name);
				}
			});
		}
	}
	
	public Collection<PropertyDefinition<P, ?>> getProperties() {
		return properties.values();
	}
	
	@SuppressWarnings("serial")
	private static class PojoGeneratorNotFoundException extends RuntimeException {
		public PojoGeneratorNotFoundException(Throwable cause) {
			super("No valid POJO generator found", cause);
		}
	}
	
	@SuppressWarnings("serial")
	private static class ConstructorCannotGeneratePojoException extends RuntimeException {
		public ConstructorCannotGeneratePojoException(Throwable cause) {
			super("Cannot use constructor as POJO generator", cause);
		}
	}
	
	@SuppressWarnings("serial")
	private static class InterfaceClassException extends RuntimeException {
		public InterfaceClassException(Class<?> interfaceClass) {
			super("Not a class, but an interface: " + interfaceClass);
		}
	}
	
	@SuppressWarnings("serial")
	private static class NonInterfaceClassException extends RuntimeException {
		public NonInterfaceClassException(Class<?> nonInterfaceClass) {
			super("Not an interface: " + nonInterfaceClass);
		}
	}
}
