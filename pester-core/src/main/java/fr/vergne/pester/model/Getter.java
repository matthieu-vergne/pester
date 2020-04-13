package fr.vergne.pester.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Option;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.util.argscheck.ArgsCheck;
import fr.vergne.pester.util.namer.Namer;

public class Getter<P> implements DefinitionSourcer {
	private final Class<P> pojoClass;
	private final Optional<String> expectedName;
	private final Predicate<Method> methodPredicate;
	private final String defaultName;
	private final StackTraceElement[] definitionStackTrace;

	public Getter(Class<P> pojoClass, Namer namer) {
		this.pojoClass = ArgsCheck.requireNonNull(pojoClass, "No POJO class provided");
		ArgsCheck.requireNonNull(namer, "No namer provided");
		this.expectedName = namer.getExpectedName();
		this.methodPredicate = method -> method.getParameters().length == 0 && namer.getNamePredicate().test(method.getName());
		this.defaultName = namer.getDefaultName();

		this.definitionStackTrace = createDefinitionStackTraceFromHere();
	}

	@Override
	public StackTraceElement[] getDefinitionStackTrace() {
		return definitionStackTrace;
	}

	public boolean isPresent() {
		return searchMethod().isPresent();
	}

	public Class<?> getReturnClass() {
		return retrieveMethod().getReturnType();
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

	public Object getFrom(P pojo) {
		Method method = retrieveMethod();
		method.setAccessible(true);
		try {
			return method.invoke(pojo);
		} catch (InvocationTargetException cause) {
			throw new DefinitionUnfulfilledException(getSignature() + " throws an exception", cause.getCause());
		} catch (IllegalAccessException cause) {
			throw new ShouldNotOccurException(cause);
		}
	}

	private Method retrieveMethod() {
		return searchMethod().orElseThrow(() -> new DefinitionUnfulfilledException(getSignature() + " not found"));
	}

	@Override
	public String toString() {
		return getSignature();
	}

	private String getSignature() {
		return searchMethodName().map(name -> name + "()").orElse(defaultName);
	}

	private Optional<Method> searchMethod() {
		return Stream.of(pojoClass.getDeclaredMethods()).filter(methodPredicate).findAny();
	}

	private Optional<String> searchMethodName() {
		if (expectedName.isPresent()) {
			return expectedName;
		} else {
			return searchMethod().map(Method::getName);
		}
	}
}
