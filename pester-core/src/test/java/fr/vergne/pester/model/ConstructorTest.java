package fr.vergne.pester.model;

import static fr.vergne.pester.testutil.ArgumentsSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import fr.vergne.pester.factory.Factory;
import fr.vergne.pester.factory.TypeFactory;
import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.testutil.Method;
import fr.vergne.pester.value.Type;

@SuppressWarnings("unused")
class ConstructorTest {

	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoPojoClass(Class<?> pojoClass) {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Constructor<>(pojoClass, Collections.emptyList()));
		assertEquals("No POJO class provided", exception.getMessage());
	}

	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoParameterTypes(List<Type<?>> parameterTypes) {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Constructor<>(TestClass.class, parameterTypes));
		assertEquals("No list of parameter types provided, should be at least an empty list", exception.getMessage());
	}

	static class Custom {
	}
	
	static class TestClass {
		TestClass() {
		}

		TestClass(byte b, Integer i, Custom c, List<Object> l) {
		}
	}
	
	static Stream<Arguments> testIsPresentReturnsTrueWhenPresent() {
		Class<?>[] args = {byte.class, Integer.class, Custom.class, List.class};
		String argsSignature = "TestClass(byte, Integer, Custom, List<Object>)";
		
		return Stream.of(Arguments.of(TestClass.class, types(), "TestClass()"),
				Arguments.of(TestClass.class, types(ConstructorTest::classType, args), argsSignature),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, args), argsSignature),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, args), argsSignature));
	}

	@ParameterizedTest(name = "Constructor{1}")
	@MethodSource
	void testIsPresentReturnsTrueWhenPresent(Class<TestClass> pojoClass, List<Type<?>> parameterTypes, String signature) {
		Constructor<TestClass> constructor = new Constructor<>(pojoClass, parameterTypes);
		assertTrue(constructor.isPresent(), signature + " not found");
	}
	
	static Stream<Arguments> testIsPresentReturnsFalseWhenAbsent() {
		Class<?>[] args = {byte.class, Integer.class, Custom.class};
		String argsSignature = "TestClass(byte, Integer, Custom)";
		
		return Stream.of(Arguments.of(TestClass.class, types(ConstructorTest::classType, args), argsSignature),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, args), argsSignature),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, args), argsSignature));
	}

	@ParameterizedTest(name = "Constructor{1}")
	@MethodSource
	void testIsPresentReturnsFalseWhenAbsent(Class<TestClass> pojoClass, List<Type<?>> parameterTypes, String signature) {
		Constructor<TestClass> constructor = new Constructor<>(pojoClass, parameterTypes);
		assertFalse(constructor.isPresent(), signature + " found");
	}

	static Stream<Arguments> testGetVisibilityReturnsActualVisibility() {
		class VisibilityTestClass {
			private VisibilityTestClass(byte b) {
			}
			protected VisibilityTestClass(int i) {
			}
			VisibilityTestClass(long l) {
			}
			public VisibilityTestClass(double d) {
			}
		}
		return Stream.of(Arguments.of(VisibilityTestClass.class, byte.class, Visibility.PRIVATE),
				Arguments.of(VisibilityTestClass.class, int.class, Visibility.PROTECTED),
				Arguments.of(VisibilityTestClass.class, long.class, Visibility.PACKAGE),
				Arguments.of(VisibilityTestClass.class, double.class, Visibility.PUBLIC))
				.flatMap(replaceArg(1, arg -> createConstrainedTypes((Class<?>) arg)));
	}

	@ParameterizedTest(name = "constructor[{1}] is {2}")
	@MethodSource
	void testGetVisibilityReturnsActualVisibility(Class<?> pojoClass, Type<?> parameterType, Visibility expectedVisibility) {
		Constructor<?> setter = new Constructor<>(pojoClass, Arrays.asList(parameterType));
		assertEquals(expectedVisibility, setter.getVisibility());
	}

	static Stream<Arguments> testInvokeFailsOnMissingValue() {
		Class<?>[] args = {byte.class, Integer.class, Custom.class, List.class};
		List<Object> argsValues = Arrays.asList((byte) 0, (Integer) 1, new Custom());
		
		return Stream.of(Arguments.of(TestClass.class, types(ConstructorTest::classType, args), argsValues),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, args), argsValues),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, args), argsValues));
	}

	@ParameterizedTest(name = "Constructor{1} with values {2}")
	@MethodSource
	<P> void testInvokeFailsOnMissingValue(Class<P> pojoClass, List<Type<?>> parameterTypes, List<Object> arguments) {
		Constructor<P> constructor = new Constructor<>(pojoClass, parameterTypes);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> constructor.invoke(arguments));
		assertEquals("wrong number of arguments", exception.getMessage());
	}

	static Stream<Arguments> testInvokeFailsOnExtraValue() {
		Class<?>[] args = {byte.class, Integer.class, Custom.class, List.class};
		List<Object> argsValues = Arrays.asList((byte) 0, (Integer) 1, new Custom(), Collections.emptyList(), "extra");
		
		return Stream.of(Arguments.of(TestClass.class, types(), Arrays.asList("extra")),
				Arguments.of(TestClass.class, types(ConstructorTest::classType, args), argsValues),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, args), argsValues),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, args), argsValues));
	}

	@ParameterizedTest(name = "Constructor{1} with values {2}")
	@MethodSource
	<P> void testInvokeFailsOnExtraValue(Class<P> pojoClass, List<Type<?>> parameterTypes, List<Object> arguments) {
		Constructor<P> constructor = new Constructor<>(pojoClass, parameterTypes);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> constructor.invoke(arguments));
		assertEquals("wrong number of arguments", exception.getMessage());
	}

	@SuppressWarnings("unchecked")
	static Stream<Arguments> testInvokeFailsDefinitionOnInvalidValueType() {
		Class<?>[] argClasses = {byte.class, Integer.class, Custom.class, List.class};
		List<Object> argValues = Arrays.asList((byte) 0, (Integer) 1, "foo", Collections.emptyList());
		
		return Stream.of(Arguments.of(TestClass.class, types(ConstructorTest::classType, argClasses)),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, argClasses)),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, argClasses)))
				.flatMap(appendArg(argValues))
				.flatMap(appendArgReadAll(args -> Stream.of(createSignature(TestClass.class, (List<Type<?>>) args[1]))));
	}

	@ParameterizedTest(name = "Constructor{1} rejects values {2}")
	@MethodSource
	<P> void testInvokeFailsDefinitionOnInvalidValueType(Class<P> pojoClass, List<Type<?>> parameterTypes,
			List<Object> arguments, String signature) {
		Constructor<P> constructor = new Constructor<>(pojoClass, parameterTypes);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> constructor.invoke(arguments));
		assertEquals(signature + " does not accept one of " + arguments, exception.getMessage());
		assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
		assertEquals("argument type mismatch", exception.getCause().getMessage());
	}
	
	@SuppressWarnings("serial")
	static class TestException extends RuntimeException {}
	static final TestException exception = new TestException();
	static class ExceptionClass {
		
		ExceptionClass() {
			throw exception;
		}
		
		ExceptionClass(byte b, Integer i, Custom c, List<Object> l) {
			throw exception;
		}
	}
	
	@SuppressWarnings("unchecked")
	static Stream<Arguments> testInvokeFailsDefinitionOnException() {
		Class<?>[] argClasses = {byte.class, Integer.class, Custom.class, List.class};
		List<Object> argValues = Arrays.asList((byte) 0, (Integer) 1, new Custom(), Collections.emptyList());
		
		return Stream.of(Arguments.of(ExceptionClass.class, types(), Collections.emptyList()),
				Arguments.of(ExceptionClass.class, types(ConstructorTest::classType, argClasses), argValues),
				Arguments.of(ExceptionClass.class, types(ConstructorTest::unconstrainedType, argClasses), argValues),
				Arguments.of(ExceptionClass.class, types(ConstructorTest::constrainedType, argClasses), argValues))
				.flatMap(appendArgReadAll(args -> Stream.of(createSignature(ExceptionClass.class, (List<Type<?>>) args[1]))))
				.flatMap(appendArg(() -> Stream.of(exception)));
	}

	@ParameterizedTest(name = "Constructor{1} with values {2}")
	@MethodSource
	<P> void testInvokeFailsDefinitionOnException(Class<P> pojoClass, List<Type<?>> parameterTypes,
			List<Object> arguments, String signature, Exception expectedCause) {
		Constructor<P> constructor = new Constructor<>(pojoClass, parameterTypes);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> constructor.invoke(arguments));
		assertEquals(signature + " throws an exception", exception.getMessage());
		assertEquals(expectedCause, exception.getCause());
	}
	
	static Stream<Arguments> testInvokeInstantiatesPojo() {
		Class<?>[] argClasses = {byte.class, Integer.class, Custom.class, List.class};
		List<Object> argValues = Arrays.asList((byte) 0, (Integer) 1, new Custom(), Collections.emptyList());
		
		return Stream.of(Arguments.of(TestClass.class, types(), Collections.emptyList()),
				Arguments.of(TestClass.class, types(ConstructorTest::classType, argClasses), argValues),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, argClasses), argValues),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, argClasses), argValues));
	}

	@ParameterizedTest(name = "Constructor{1} with values {2}")
	@MethodSource
	<P> void testInvokeInstantiatesPojo(Class<P> pojoClass, List<Type<?>> parameterTypes, List<Object> arguments) {
		Constructor<P> constructor = new Constructor<>(pojoClass, parameterTypes);
		P pojo = constructor.invoke(arguments);
		assertNotNull(pojo);
	}
	
	@SuppressWarnings("unchecked")
	static Stream<Arguments> testToStringReturnsConstructorSignature() {
		Class<?>[] argClasses = {byte.class, Integer.class, Custom.class, List.class};
		
		return Stream.of(Arguments.of(TestClass.class, types()),
				Arguments.of(TestClass.class, types(ConstructorTest::classType, argClasses)),
				Arguments.of(TestClass.class, types(ConstructorTest::unconstrainedType, argClasses)),
				Arguments.of(TestClass.class, types(ConstructorTest::constrainedType, argClasses)))
				.flatMap(appendArgReadAll(args -> Stream.of(createSignature(TestClass.class, (List<Type<?>>) args[1]))));
	}

	@ParameterizedTest(name = "constructor{1} returns {2}")
	@MethodSource
	void testToStringReturnsConstructorSignature(Class<TestClass> pojoClass, List<Type<?>> parameterTypes, String signature) {
		Constructor<TestClass> setter = new Constructor<>(pojoClass, parameterTypes);
		assertEquals(signature, setter.toString());
	}
	
	static Stream<Arguments> testMethodFailsDefinitionWhenAbsent() throws NoSuchMethodException, SecurityException {
		return Stream.of(
				Arguments.of(new Method(Constructor.class, "getVisibility")),
				Arguments.of(new Method(Constructor.class, "invoke", Arrays.asList("foo"))));
	}

	@ParameterizedTest
	@MethodSource
	void testMethodFailsDefinitionWhenAbsent(Method method) {
		List<Type<?>> parameterTypes = Arrays.asList(typeFactory.from(String.class));
		Constructor<?> constructor = new Constructor<>(TestClass.class, parameterTypes);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> method.invoke(constructor));
		assertEquals(createSignature(TestClass.class, parameterTypes) + " not found", exception.getMessage());
	}

	static Stream<Arguments> testMethodFailsIfSeveralCandidates() throws NoSuchMethodException, SecurityException {
		class MultiCandidatesTestClass {
			MultiCandidatesTestClass(byte value) {} // Targeted constructor
			MultiCandidatesTestClass(int value) {}  // Equivalent constructor but for parameter class
		}
		// Parameter class not constrained
		List<Type<?>> parameterTypes = Arrays.asList(new Factory().type().as("unconstrainedType"));
		
		List<String> candidates = Arrays.asList(
				MultiCandidatesTestClass.class.getDeclaredConstructor(byte.class).toString(),
				MultiCandidatesTestClass.class.getDeclaredConstructor(int.class).toString());
		
		return Stream.of(
				Arguments.of(new Method(Constructor.class, "isPresent")),
				Arguments.of(new Method(Constructor.class, "getVisibility")),
				Arguments.of(new Method(Constructor.class, "invoke", Arrays.asList("foo"))))
				.flatMap(appendArg(candidates))
				.flatMap(appendArg(MultiCandidatesTestClass.class))
				.flatMap(appendArg(parameterTypes));
	}

	@ParameterizedTest(name = "{0} find {1}")
	@MethodSource
	void testMethodFailsIfSeveralCandidates(Method method, List<String> candidates, Class<?> pojoClass,
			List<Type<?>> parameterTypes) {
		Constructor<?> constructor = new Constructor<>(pojoClass, parameterTypes);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> method.invoke(constructor));
		assertEquals("More than one constructor found, consider setting classes for " + parameterTypes + ": " + candidates, exception.getMessage());
	}

	static final TypeFactory typeFactory = new Factory().type();

	static <T> Type<T> classType(Class<T> typeClass) {
		return typeFactory.from(typeClass);
	}

	static <T> Type<T> unconstrainedType(Class<T> typeClass) {
		return typeFactory.as("type[]");
	}

	static <T> Type<T> constrainedType(Class<T> typeClass) {
		return typeFactory.as("type[" + typeClass.getSimpleName() + "]", typeClass);
	}	
	
	private static List<Type<?>> types() {
		return Collections.emptyList();
	}

	private static List<Type<?>> types(Function<Class<?>, Type<?>> mapper, Class<?>... classes) {
		return Stream.of(classes).map(mapper).collect(Collectors.toList());
	}

	private static Stream<Type<? extends Object>> createConstrainedTypes(Class<?> typeClass) {
		return Stream.of(classType(typeClass), constrainedType(typeClass));
	}

	private static <P> String createSignature(Class<P> pojoClass, List<Type<?>> parameterTypes) {
		String argsString = parameterTypes.stream().map(Type::getName).collect(Collectors.joining(", "));
		String constructorName = pojoClass.getSimpleName();
		return constructorName + "(" + argsString + ")";
	}

	private static Stream<Object> toArray(List<?> list) {
		return Stream.of((Object) list.toArray());
	}
}
