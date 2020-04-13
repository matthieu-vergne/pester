package fr.vergne.pester.model;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.vergne.pester.options.Option;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.argscheck.ArgsCheck;
import fr.vergne.pester.util.indexer.IndexedValue;
import fr.vergne.pester.util.indexer.Indexer;
import fr.vergne.pester.util.indexer.impl.IteratorIndexer;
import fr.vergne.pester.value.Type;

public class Constructor<P> implements DefinitionSourcer {
	
	private final Class<P> pojoClass;
	private final List<Type<?>> parameterTypes;
	private final Predicate<java.lang.reflect.Constructor<?>> candidatePredicate;
	private final String signature;
	private final StackTraceElement[] definitionStackTrace;

	public Constructor(Class<P> pojoClass, List<Type<?>> parameterTypes) {
		this.pojoClass = ArgsCheck.requireNonNull(pojoClass, "No POJO class provided");
		this.parameterTypes = ArgsCheck.requireNonNull(parameterTypes, "No list of parameter types provided, should be at least an empty list");
		
		int parameterCount = parameterTypes.size();
		Map<Integer, Class<?>> expectedClasses = retrieveExpectedClasses(parameterTypes);
		this.candidatePredicate = constructor -> constructor.getParameterCount() == parameterCount
				&& checkClasses(expectedClasses, constructor.getParameterTypes());
		
		this.signature = pojoClass.getSimpleName() + "("
				+ parameterTypes.stream().map(Type::getName).collect(Collectors.joining(", "))
				+ ")";
		
		this.definitionStackTrace = createDefinitionStackTraceFromHere();
	}

	private Map<Integer, Class<?>> retrieveExpectedClasses(List<Type<?>> parameterTypes) {
		Indexer<Class<?>> indexer = new IteratorIndexer<>();
		return parameterTypes.stream()
				.map(type -> type.getTypeClass().orElse(null))
				.map(indexer::decorateWithIndex)
				.filter(indexedValue -> indexedValue.getValue() != null)
				.collect(Collectors.toMap(IndexedValue::getIndex, IndexedValue::getValue));
	}
	
	private boolean checkClasses(Map<Integer, Class<?>> expectedClasses, Class<?>[] actualClasses) {
		return expectedClasses.entrySet().stream()
			.map(entry -> entry.getValue().equals(actualClasses[entry.getKey()]))
			.filter(satisfied -> !satisfied)
			.findAny()
			.orElse(true);
	}
	
	@Override
	public StackTraceElement[] getDefinitionStackTrace() {
		return definitionStackTrace;
	}

	public boolean isPresent() {
		return searchConstructor().isPresent();
	}
	
	public Visibility getVisibility() {
		return Option.getFromModifiers(retrieveConstructor().getModifiers(), Visibility.class);
	}

	public P invoke(List<?> arguments) {
		java.lang.reflect.Constructor<P> constructor = retrieveConstructor();
		constructor.setAccessible(true);
		try {
			return constructor.newInstance(arguments.toArray());
		} catch (InvocationTargetException cause) {
			throw new DefinitionUnfulfilledException(signature + " throws an exception", cause.getCause());
		} catch (IllegalArgumentException cause) {
			if ("argument type mismatch".equals(cause.getMessage())) {
				throw new DefinitionUnfulfilledException(signature + " does not accept one of " + arguments, cause);
			} else {
				throw cause;
			}
		} catch (InstantiationException | IllegalAccessException cause) {
			throw new ShouldNotOccurException(cause);
		}
	}

	private java.lang.reflect.Constructor<P> retrieveConstructor() {
		return searchConstructor().orElseThrow(() -> new DefinitionUnfulfilledException(signature + " not found"));
	}

	private Optional<java.lang.reflect.Constructor<P>> searchConstructor() {
		@SuppressWarnings("unchecked")
		List<java.lang.reflect.Constructor<P>> candidates = Stream.of(pojoClass.getDeclaredConstructors())
				.filter(candidatePredicate)
				.map(constructor -> (java.lang.reflect.Constructor<P>) constructor)
				.sorted((m1, m2) -> m1.toString().compareTo(m2.toString()))
				.collect(Collectors.toList());
		
		if (candidates.size() > 1) {
			throw new DefinitionUnfulfilledException("More than one constructor found, consider setting classes for " + parameterTypes + ": " + candidates);
		} else {
			return candidates.stream().findAny();
		}
	}

	@Override
	public String toString() {
		return signature;
	}
}
