package fr.vergne.pester.junit;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import fr.vergne.pester.definition.ConstructorDefinition;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;
import fr.vergne.pester.value.Nullable;

public class TestCasesGenerator {
	
	public static Stream<Arguments> streamTestCases(PojoDefinition<?> pojoDefinition, TestParameter[] testParameters) {
		return Stream.of(pojoDefinition)
				.flatMap(toDefinitionItems())
				.map(toOptionalArguments(testParameters))
				.filter(hasAllArguments())
				.distinct()
				.map(toTestArguments());
	}
	
	private static <P> Function<PojoDefinition<P>, Stream<DefinitionItem<P>>> toDefinitionItems() {
		return pojoDefinition -> {
			// Retrieve the various data sources
			Collection<Class<?>> parentClasses = new LinkedList<>(Arrays.asList(pojoDefinition.getParentClass().orElse(null)));
			Collection<Class<?>> interfaceClasses = new LinkedList<>(pojoDefinition.getInterfaces());
			Collection<ConstructorDefinition<P>> constructors = new LinkedList<>(pojoDefinition.getConstructors());
			Collection<PropertyDefinition<P, ?>> properties = new LinkedList<>(pojoDefinition.getProperties());
			
			// Ensure each of them has at least one element
			Stream.of(parentClasses, interfaceClasses, constructors, properties)
					.filter(collection -> collection.isEmpty())
					.forEach(collection -> collection.add(null));
			
			// Generate the combinations
			// The extra null prevents from having zero data because of an empty source
			return parentClasses.stream()
					.flatMap(parentClass -> interfaceClasses.stream()
						.flatMap(interfaceClass -> constructors.stream()
								.flatMap(constructorDefinition -> properties.stream()
										.map(propertyDefinition -> new DefinitionItem<>(
											Optional.ofNullable(pojoDefinition),
											Optional.ofNullable(constructorDefinition),
											Optional.ofNullable(propertyDefinition),
											Optional.ofNullable(parentClass),
											Optional.ofNullable(interfaceClass))))));
		};
	}
	
	private static Function<List<Optional<?>>, Arguments> toTestArguments() {
		return arguments -> Arguments.of(arguments.stream()
				.map(Optional::get)
				.map(nullableToValue())
				.toArray());
	}
	
	private static Predicate<List<Optional<?>>> hasAllArguments() {
		return arguments -> arguments.stream().allMatch(Optional::isPresent);
	}
	
	private static Function<DefinitionItem<?>, List<Optional<?>>> toOptionalArguments(TestParameter[] testParameters) {
		return definitionItem -> Stream.of(testParameters)
				.map(parameter -> parameter.extractFrom(definitionItem))
				.map(nonOptionalToOptional())
				.collect(Collectors.toList());
	}
	
	private static Function<Object, Optional<?>> nonOptionalToOptional() {
		return value -> value instanceof Optional ? (Optional<?>) value : Optional.of(value);
	}
	
	private static Function<Object, Object> nullableToValue() {
		return value -> value instanceof Nullable ? ((Nullable<?>) value).get() : value;
	}
}
