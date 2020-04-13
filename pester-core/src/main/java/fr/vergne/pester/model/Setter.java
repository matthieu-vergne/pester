package fr.vergne.pester.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Option;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.argscheck.ArgsCheck;
import fr.vergne.pester.util.namer.Namer;
import fr.vergne.pester.value.Type;

public class Setter<P> implements DefinitionSourcer {
	private final Class<P> pojoClass;
	private final Type<?> parameterType;
	private final StackTraceElement[] definitionStackTrace;
	private final Predicate<Method> methodPredicate;
	private final Optional<String> expectedName;
	private final String defaultName;

	public Setter(Class<P> pojoClass, Type<?> parameterType, Namer namer) {
		this.pojoClass = ArgsCheck.requireNonNull(pojoClass, "No POJO class provided");
		this.parameterType = ArgsCheck.requireNonNull(parameterType, "No parameter type provided");
		ArgsCheck.requireNonNull(namer, "No namer provided");
		this.expectedName = namer.getExpectedName();
		this.defaultName = namer.getDefaultName();
		
		Predicate<Method> predicate = method -> namer.getNamePredicate().test(method.getName())
				&& method.getParameters().length == 1;
		Optional<Class<?>> paramClass = parameterType.getTypeClass();
		if (paramClass.isPresent()) {
			Class<?> pc = paramClass.get();
			predicate = predicate.and(method -> method.getParameters()[0].getType().equals(pc));
		}
		this.methodPredicate = predicate;

		this.definitionStackTrace = createDefinitionStackTraceFromHere();
	}

	@Override
	public StackTraceElement[] getDefinitionStackTrace() {
		return definitionStackTrace;
	}

	public boolean isPresent() {
		return searchMethod().isPresent();
	}

	public Visibility getVisibility() {
		return Option.getFromModifiers(retrieveMethod().getModifiers(), Visibility.class);
	}

	public Mutability getMutability() {
		return Option.getFromModifiers(retrieveMethod().getModifiers(), Mutability.class);
	}

	public Scope getScope() {
		return Option.getFromModifiers(retrieveMethod().getModifiers(), Scope.class);
	}

	public void setTo(P pojo, Object value) {
		Method method = retrieveMethod();
		method.setAccessible(true);
		try {
			method.invoke(pojo, value);
		} catch (IllegalArgumentException cause) {
			throw new DefinitionUnfulfilledException(getSignature() + " does not accept '" + value + "'", cause);
		} catch (InvocationTargetException cause) {
			throw new DefinitionUnfulfilledException(getSignature() + " throws an exception", cause.getCause());
		} catch (IllegalAccessException cause) {
			throw new ShouldNotOccurException(cause);
		}
	}

	public Class<?> getParameterClass() {
		return retrieveMethod().getParameterTypes()[0];
	}

	private Method retrieveMethod() {
		return searchMethod().orElseThrow(() -> new DefinitionUnfulfilledException(getSignature() + " not found"));
	}

	private Optional<Method> searchMethod() {
		List<Method> candidates = Stream.of(pojoClass.getDeclaredMethods())
				.filter(methodPredicate)
				.sorted((m1, m2) -> m1.toString().compareTo(m2.toString()))
				.collect(Collectors.toList());
		
		if (candidates.size() > 1) {
			throw new DefinitionUnfulfilledException(
					"More than one setter found, consider setting a class for " + parameterType + ": " + candidates);
		} else {
			return candidates.stream().findAny();
		}
	}

	@Override
	public String toString() {
		return getSignature();
	}

	private String getSignature() {
		return searchMethodName().map(name -> name + "(" + parameterType + ")").orElse(defaultName);
	}

	private Optional<String> searchMethodName() {
		if (expectedName.isPresent()) {
			return expectedName;
		} else {
			return searchMethod().map(Method::getName);
		}
	}
}
