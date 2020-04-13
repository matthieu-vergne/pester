package fr.vergne.pester.definition;

import static fr.vergne.pester.options.Mutability.*;
import static fr.vergne.pester.options.Scope.*;
import static fr.vergne.pester.options.Visibility.*;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.platform.commons.support.AnnotationSupport;

import fr.vergne.pester.factory.Factory;
import fr.vergne.pester.junit.annotation.ConstructorPropertiesHelper;
import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Option;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.indexer.Indexer;
import fr.vergne.pester.util.indexer.impl.IteratorIndexer;
import fr.vergne.pester.value.Type;

public class DefinitionFactory {
	
	private static final Logger LOGGER = Logger.getLogger(DefinitionFactory.class.getName());
	
	private final Factory factory = new Factory();

	/**
	 * Create a {@link PojoDefinition} based on what can be retrieved from the given
	 * {@link Class}, aiming for activating as much tests as possible. It is a
	 * simple way to generate a sample test report.
	 * <p>
	 * <b>This method is not intended to provide reliable test suite</b>, since any
	 * change in the implementation means changing the set of tests to be activated.
	 * 
	 * @param pojoClass the {@link Class} to investigate
	 * @return the {@link PojoDefinition} of this {@link Class}
	 */
	public <P> PojoDefinition<P> guessFromClass(Class<P> pojoClass) {
		PojoDefinition<P> def = new PojoDefinition<>(pojoClass, factory);
		Ext<P> ext = new Ext<>(def);
		
		createFieldStream(pojoClass)
		.forEach(data -> ext.addOrGetProperty(data.type, data.propertyName).withField(data.options));
		createGetterStream(pojoClass)
		.forEach(data -> ext.addOrGetProperty(data.type, data.propertyName).withGetter(data.options));
		createSetterStream(pojoClass)
		.forEach(data -> ext.addOrGetProperty(data.type, data.propertyName).withSetter(data.options));
		createConstructorStream(pojoClass)
		.forEach(constructorData -> {
			@SuppressWarnings("unchecked")
			PropertyDefinition<P, ?>[] properties = constructorData.propertiesData.stream()
					.map(propertyData -> ext.addOrGetProperty(propertyData.type, propertyData.propertyName))
					.toArray(length -> new PropertyDefinition[length]);
			def.addConstructor(constructorData.visibility, properties);
		});
		return def;
	}
	
	/**
	 * Create a {@link PojoDefinition} assuming the given {@link Class} must follow
	 * the JavaBean specification:
	 * <ul>
	 * <li>The class must have a public default constructor (with no arguments).
	 * <li>The class properties must be accessible using a getter and a setter.
	 * <li>The class should be {@link Serializable}.
	 * </ul>
	 * 
	 * @param pojoClass the JavaBean {@link Class} to investigate
	 * @return the {@link PojoDefinition} of this JavaBean {@link Class}
	 */
	public <P> PojoDefinition<P> fromBeanClass(Class<P> pojoClass) {
		PojoDefinition<P> def = new PojoDefinition<>(pojoClass, factory);
		Ext<P> ext = new Ext<>(def);
		
		def.addInterfaces(Serializable.class);
		
		def.addConstructor(PUBLIC);
		
		createFieldStream(pojoClass)
		.forEach(data -> {
			ext.addOrGetProperty(data.type, data.propertyName).withField(NON_STATIC, NON_FINAL)
					.withGetter(PUBLIC, NON_STATIC)
					.withSetter(PUBLIC, NON_STATIC);
		});
		
		createGetterStream(pojoClass)
		.forEach(data -> {
			try {
				def.addProperty(data.type, data.propertyName)
					.withGetter(PUBLIC, NON_STATIC)
					.withSetter(PUBLIC, NON_STATIC);
			} catch (AlreadyDefinedPropertyException cause) {
				// Already known property, so already completely defined
			}
		});
		createSetterStream(pojoClass)
		.forEach(data -> {
			try {
				def.addProperty(data.type, data.propertyName)
					.withGetter(PUBLIC, NON_STATIC)
					.withSetter(PUBLIC, NON_STATIC);
			} catch (AlreadyDefinedPropertyException cause) {
				// Already known property, so already completely defined
			}
		});
		
		return def;
	}
	
	private <P> Stream<PropertyData> createFieldStream(Class<P> pojoClass) {
		return Stream.of(pojoClass.getDeclaredFields())
				.filter(field -> !field.isSynthetic())
				.map(this::extractFieldData);
	}

	private <P> Stream<PropertyData> createGetterStream(Class<P> pojoClass) {
		return Stream.of(pojoClass.getDeclaredMethods())
				.filter(method -> !method.isSynthetic())
				.filter(DefinitionFactory::hasGetterName)
				.filter(DefinitionFactory::hasReturnType)
				.filter(DefinitionFactory::hasNoParameter)
				.map(this::extractGetterData);
	}

	private <P> Stream<PropertyData> createSetterStream(Class<P> pojoClass) {
		return Stream.of(pojoClass.getDeclaredMethods())
				.filter(method -> !method.isSynthetic())
				.filter(DefinitionFactory::hasSetterName)
				.filter(DefinitionFactory::hasNoReturnType)
				.filter(DefinitionFactory::hasSingleParameter)
				.map(this::extractSetterData);
	}

	private <P> Stream<ConstructorData> createConstructorStream(Class<P> pojoClass) {
		return Stream.of(pojoClass.getDeclaredConstructors())
				.filter(constructor -> !constructor.isSynthetic())
				.map(this::extractConstructorData);
	}

	private static boolean hasNoParameter(Method method) {
		return method.getParameterTypes().length == 0;
	}

	private static boolean hasSingleParameter(Method method) {
		return method.getParameterTypes().length == 1;
	}

	private static boolean hasReturnType(Method method) {
		return !hasNoReturnType(method);
	}

	private static boolean hasNoReturnType(Method method) {
		return method.getReturnType().equals(void.class);
	}

	private static final Pattern GETTER_PATTERN = Pattern.compile("^get[A-Z]");
	private static boolean hasGetterName(Method method) {
		return GETTER_PATTERN.matcher(method.getName()).find();
	}

	private static final Pattern SETTER_PATTERN = Pattern.compile("^set[A-Z]");
	private static boolean hasSetterName(Method method) {
		return SETTER_PATTERN.matcher(method.getName()).find();
	}

	private class PropertyData {
		private final Optional<String> propertyName;
		private final Type<?> type;
		private final Option[] options;
		
		public PropertyData(Optional<String> rawName, Class<?> typeClass, Option... options) {
			this(rawName, UnaryOperator.identity(), typeClass, options);
		}
		
		public PropertyData(Optional<String> rawName, UnaryOperator<String> propertyNamer, Class<?> typeClass, Option... options) {
			this.propertyName = rawName.map(propertyNamer);
			this.type = factory.type().from(typeClass);
			this.options = options;
		}
	}
	
	private static class ConstructorData {
		
		private final Visibility visibility;
		private final List<PropertyData> propertiesData;

		public ConstructorData(List<PropertyData> propertiesData, Visibility visibility) {
			this.propertiesData = propertiesData;
			this.visibility = visibility;
		}
	}
	
	private PropertyData extractFieldData(Field field) {
		return new PropertyData(
				Optional.of(field.getName()),
				field.getType(),
				extractOptions(field::getModifiers));
	}

	private static Option[] extractOptions(Supplier<Integer> modifiersExtractor) {
		return Stream.of(
				Visibility.class,
				Mutability.class,
				Scope.class)
				.map(optionClass -> findOption(optionClass, modifiersExtractor))
				.toArray(length -> new Option[length]);
	}

	private static <T extends Option> T findOption(Class<T> optionClass, Supplier<Integer> modifiersExtractor) {
		return Stream.of(optionClass.getEnumConstants())
				.filter(v -> v.testModifiers(modifiersExtractor.get()))
				.findFirst()
				.get();
	}
	
	private PropertyData extractGetterData(Method method) {
		return new PropertyData(
				Optional.of(method.getName()),
				t -> uncapitalize(method.getName().substring("get".length())),
				method.getReturnType(),
				extractOptions(method::getModifiers));
	}
	
	private PropertyData extractSetterData(Method method) {
		return new PropertyData(
				Optional.of(method.getName()),
				t -> uncapitalize(method.getName().substring("set".length())),
				method.getParameterTypes()[0],
				extractOptions(method::getModifiers));
	}
	
	private ConstructorData extractConstructorData(Constructor<?> constructor) {
		Indexer<Parameter> indexer = new IteratorIndexer<Parameter>();
		List<PropertyData> propertiesData = Stream.of(constructor.getParameters())
				.map(indexer::decorateWithIndex)
				.map(indexedParameter -> {
					int index = indexedParameter.getIndex();
					Parameter parameter = indexedParameter.getValue();
					return new PropertyData(
							retrieveParameterProperty(constructor, parameter, index),
							parameter.getType());
				})
				.collect(Collectors.toList());
		
		if (hasMissingNames(propertiesData)) {
			// TODO Try to figure out relations with other properties by playing with the instance
			LOGGER.warning("Parameter names missing for the constructor " + constructor + "."
					+ " We cannot infer whether these parameters are linked to other properties."
					+ " No test on the effects of the constructor on properties will be run."
					+ " To run these tests, you need to do one of the following:"
					+ " (i) add the required compiler options to store the parameter names,"
					+ " (ii) annotate the constructor with @ConstructorProperties,"
					+ " (iii) add the constructor manually,"
					+ " (iv) add your own test.");
		}
		
		Option[] options = extractOptions(constructor::getModifiers);
		Visibility visibility = Stream.of(options).filter(Visibility.class::isInstance).map(Visibility.class::cast).findAny().get();
		return new ConstructorData(propertiesData, visibility);
	}

	private static Optional<String> retrieveParameterProperty(Constructor<?> constructor, Parameter parameter, int index) {
		Optional<ConstructorProperties> annotation = AnnotationSupport.findAnnotation(constructor, ConstructorProperties.class);
		if (annotation.isPresent()) {
			return Optional.of(annotation.get().value()[index]);
		} else if (parameter.isNamePresent()) {
			return Optional.of(parameter.getName());
		} else {
			return Optional.empty();
		}
	}

	private static boolean hasMissingNames(List<PropertyData> propertiesData) {
		return propertiesData.stream()
				.filter(data -> !data.propertyName.isPresent())
				.findAny().isPresent();
	}
	
	private static String uncapitalize(String noPrefixName) {
		return noPrefixName.substring(0, 1).toLowerCase() + noPrefixName.substring(1);
	}
	
	private static class Ext<P> {
		private final PojoDefinition<P> def;
		
		public Ext(PojoDefinition<P> def) {
			this.def = def;
		}
		
		public <T> PropertyDefinition<P, T> addOrGetProperty(Type<T> type, Optional<String> name) {
			return name.isPresent() ? addOrGetProperty(type, name.get()) : def.addProperty(type);
		}

		@SuppressWarnings("unchecked")
		public <T> PropertyDefinition<P, T> addOrGetProperty(Type<T> type, String name) {
			if (name.equals(ConstructorPropertiesHelper.NON_PROPERTY)) {
				return def.addProperty(type);
			} else {
				Optional<PropertyDefinition<P,?>> property = def.getProperties().stream()
						.filter(p -> p.getName().equals(name))
						.findAny();
				if (property.isPresent()) {
					PropertyDefinition<P, ?> propDef = property.get();
					if (propDef.getType().equals(type)) {
						return (PropertyDefinition<P, T>) propDef;
					} else {
						throw new IllegalArgumentException("Property " + name + " already defined as " + propDef.getType());
					}
				} else {
					return def.addProperty(type, name);
				}
			}
		}
	}
}
