package fr.vergne.pester.model;

import static fr.vergne.pester.testutil.ArgumentsSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
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
import fr.vergne.pester.util.namer.Namer;
import fr.vergne.pester.value.Type;

@SuppressWarnings("unused")
class SetterTest {

	static class TestClass {
		byte setterValue;

		void mySetter(byte value) {
			setterValue = value;
		}

		void mySetter() {
			throw new RuntimeException("this method should never be used: mono arg criteria fails");
		}

		void mySetter(byte value, byte value2) {
			throw new RuntimeException("this method should never be used: mono arg criteria fails");
		}

		void myOtherSetter(byte value) {
			throw new RuntimeException("this method should never be called: name criteria fails");
		}

		@Override
		public String toString() {
			return "pojo";// Improve test names
		}
	}
	
	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoPojoClass(Class<?> pojoClass) {
		Type<?> type = new Factory().type().from(byte.class);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Setter<>(pojoClass, type, new TestNamer("setter")));
		assertEquals("No POJO class provided", exception.getMessage());
	}

	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoType(Type<?> type) {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Setter<>(TestClass.class, type, new TestNamer("setter")));
		assertEquals("No parameter type provided", exception.getMessage());
	}

	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoNamer(Namer namer) {
		Type<?> type = new Factory().type().from(byte.class);
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Setter<>(TestClass.class, type, namer));
		assertEquals("No namer provided", exception.getMessage());
	}
	
	static Stream<Arguments> testIsPresentReturnsTrueWhenPresent() {
		return Stream.of(Arguments.of("mySetter(byte)"))
				.flatMap(appendArg(() -> createTypes("myType")))
				.flatMap(appendArg(() -> createNamers("mySetter")));
	}

	@ParameterizedTest
	@MethodSource
	void testIsPresentReturnsTrueWhenPresent(String signature, Type<?> type, Namer namer) {
		Setter<TestClass> setter = new Setter<>(TestClass.class, type, namer);
		assertTrue(setter.isPresent(), signature + " not found");
	}

	static Stream<Arguments> testIsPresentReturnsFalseWhenAbsent() {
		return Stream.of(Arguments.of("unknownSetter(byte)"))
				.flatMap(appendArg(() -> createTypes("myType")))
				.flatMap(appendArg(() -> createNamers("unknownSetter")));
	}

	@ParameterizedTest
	@MethodSource
	void testIsPresentReturnsFalseWhenAbsent(String signature, Type<?> type, Namer namer) {
		Setter<TestClass> setter = new Setter<>(TestClass.class, type, namer);
		assertFalse(setter.isPresent(), signature + " found");
	}

	static Stream<Arguments> testGetParameterClassReturnsActualParameterClass() {
		class Custom {
		}
		class ParameterTestClass {
			void primitiveSetter(byte value) {}
			void objectSetter(Integer value) {}
			void customSetter(Custom value) {}
			void genericSetter(List<Object> value) {}
		}
		
		return Stream.of(Arguments.of(ParameterTestClass.class, "myType", "primitiveSetter", byte.class),
				Arguments.of(ParameterTestClass.class, "myType", "objectSetter", Integer.class),
				Arguments.of(ParameterTestClass.class, "myType", "customSetter", Custom.class),
				Arguments.of(ParameterTestClass.class, "myType", "genericSetter", List.class))
				.flatMap(replaceArgReadAll(1, args -> createTypes((String) args[1], (Class<?>) args[3])))
				.flatMap(replaceArg(2, arg -> createNamers((String) arg)));
	}

	@ParameterizedTest(name = "{2} consumes a {3} typed with {1}")
	@MethodSource
	void testGetParameterClassReturnsActualParameterClass(Class<?> pojoClass, Type<?> type, Namer namer,
			Class<?> expectedClass) {
		Setter<?> setter = new Setter<>(pojoClass, type, namer);
		assertEquals(expectedClass, setter.getParameterClass());
	}

	static Stream<Arguments> testGetMutabilityReturnsActualMutability() {
		class MutabilityTestClass {
			void nonFinalSetter(byte value) {}
			final void finalSetter(byte value) {}
		}
		return Stream.of(Arguments.of(MutabilityTestClass.class, "nonFinalSetter", Mutability.NON_FINAL),
				Arguments.of(MutabilityTestClass.class, "finalSetter", Mutability.FINAL))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)))
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest(name = "{1} is {2}, {3}")
	@MethodSource
	void testGetMutabilityReturnsActualMutability(Class<?> pojoClass, Namer namer, Mutability expectedMutability, Type<?> type) {
		Setter<?> setter = new Setter<>(pojoClass, type, namer);
		assertEquals(expectedMutability, setter.getMutability());
	}

	static class ScopeTestClass {
		void nonStaticSetter(byte value) {}
		static void staticSetter(byte value) {}
	}

	static Stream<Arguments> testGetScopeReturnsActualScope() {
		return Stream.of(Arguments.of(ScopeTestClass.class, "nonStaticSetter", Scope.NON_STATIC),
				Arguments.of(ScopeTestClass.class, "staticSetter", Scope.STATIC))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)))
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest(name = "{1} is {2}, {3}")
	@MethodSource
	void testGetScopeReturnsActualScope(Class<?> pojoClass, Namer namer, Scope expectedScope, Type<?> type) {
		Setter<?> setter = new Setter<>(pojoClass, type, namer);
		assertEquals(expectedScope, setter.getScope());
	}

	static Stream<Arguments> testGetVisibilityReturnsActualVisibility() {
		class VisibilityTestClass {
			private void privateSetter(byte value) {}
			protected void protectedSetter(byte value) {}
			void packageSetter(byte value) {}
			public void publicSetter(byte value) {}
		}
		return Stream.of(Arguments.of(VisibilityTestClass.class, "privateSetter", Visibility.PRIVATE),
				Arguments.of(VisibilityTestClass.class, "protectedSetter", Visibility.PROTECTED),
				Arguments.of(VisibilityTestClass.class, "packageSetter", Visibility.PACKAGE),
				Arguments.of(VisibilityTestClass.class, "publicSetter", Visibility.PUBLIC))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)))
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest(name = "{1} is {2}, {3}")
	@MethodSource
	void testGetVisibilityReturnsActualVisibility(Class<?> pojoClass, Namer namer, Visibility expectedVisibility,
			Type<?> type) {
		Setter<?> setter = new Setter<>(pojoClass, type, namer);
		assertEquals(expectedVisibility, setter.getVisibility());
	}

	static Stream<Arguments> testSetToCallsSetter() {
		return createNamers("mySetter")
				.map(Arguments::of)
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest
	@MethodSource
	void testSetToCallsSetter(Namer namer, Type<?> type) {
		TestClass pojo = new TestClass();
		byte expectedValue = 123;

		Setter<TestClass> setter = new Setter<>(TestClass.class, type, namer);
		setter.setTo(pojo, expectedValue);
		Object actualValue = pojo.setterValue;
		assertEquals(expectedValue, actualValue);
	}

	static Stream<Arguments> testSetToFailsDefinitionOnException() {
		return createNamers("mySetter")
				.map(Arguments::of)
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest
	@MethodSource
	void testSetToFailsDefinitionOnException(Namer namer, Type<?> type) {
		@SuppressWarnings("serial")
		class TestException extends RuntimeException {
		}
		TestException testException = new TestException();
		class ExceptionTestClass {
			void mySetter(byte value) {throw testException;}
		}
		ExceptionTestClass pojo = new ExceptionTestClass();

		Setter<ExceptionTestClass> setter = new Setter<>(ExceptionTestClass.class, type, namer);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> setter.setTo(pojo, (byte) 123));
		assertEquals("mySetter(" + type + ") throws an exception", exception.getMessage());
		assertEquals(testException, exception.getCause());
	}

	static Stream<Arguments> testSetToFailsDefinitionOnIllegalArgument() {
		return createNamers("mySetter")
				.map(Arguments::of)
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest
	@MethodSource
	void testSetToFailsDefinitionOnIllegalArgument(Namer namer, Type<?> type) {
		TestClass pojo = new TestClass();
		Setter<TestClass> setter = new Setter<>(TestClass.class, type, namer);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> setter.setTo(pojo, "foo"));
		assertEquals("mySetter(" + type + ") does not accept 'foo'", exception.getMessage());
		assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
	}

	static Stream<Arguments> testToStringReturnsSetterSignature() {
		return createNamers("mySetter")
				.map(Arguments::of)
				.flatMap(appendArg(() -> createTypes("myType")));
	}

	@ParameterizedTest
	@MethodSource
	void testToStringReturnsSetterSignature(Namer namer, Type<?> type) {
		Setter<TestClass> setter = new Setter<>(TestClass.class, type, namer);
		assertEquals("mySetter(" + type + ")", setter.toString());
	}

	static Stream<Arguments> testMethodFailsDefinitionWhenAbsent() throws NoSuchMethodException, SecurityException {
		TestClass pojo = new TestClass();
		return Stream.of(
				Arguments.of(TestClass.class, new Method(Setter.class, "getParameterClass")),
				Arguments.of(TestClass.class, new Method(Setter.class, "getMutability")),
				Arguments.of(TestClass.class, new Method(Setter.class, "getScope")),
				Arguments.of(TestClass.class, new Method(Setter.class, "getVisibility")),
				Arguments.of(TestClass.class, new Method(Setter.class, "setTo", pojo, (byte) 123)))
				.flatMap(appendArg(() -> createTypes("myType")))
				.flatMap(appendArgReadAll(args -> createNamers("setterThatShouldNotBeDeclaredInTheClass", (Type<?>) args[2])))
				.flatMap(appendArgReadAll(args -> Stream.of("setterThatShouldNotBeDeclaredInTheClass(" + args[2] + ")")));
	}

	@ParameterizedTest(name = "{1}, {2}, {3}")
	@MethodSource
	void testMethodFailsDefinitionWhenAbsent(Class<?> pojoClass, Method method, Type<?> type, Namer namer, String signature) {
		Setter<?> setter = new Setter<>(pojoClass, type, namer);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> method.invoke(setter));
		assertEquals(signature + " not found", exception.getMessage());
	}

	static Stream<Arguments> testMethodFailsIfSeveralCandidates() throws NoSuchMethodException, SecurityException {
		class MultiCandidatesTestClass {
			void mySetter(byte value) {} // Targeted setter
			void mySetter(int value) {}  // Equivalent setter but for parameter class
		}
		Type<?> type = new Factory().type().as("unconstrainedType");// Parameter class not constrained
		
		List<String> candidates = Arrays.asList(
				MultiCandidatesTestClass.class.getDeclaredMethod("mySetter", byte.class).toString(),
				MultiCandidatesTestClass.class.getDeclaredMethod("mySetter", int.class).toString());
		
		MultiCandidatesTestClass pojo = new MultiCandidatesTestClass();
		
		return Stream.of(
				Arguments.of(MultiCandidatesTestClass.class, type, "mySetter", new Method(Setter.class, "isPresent"), candidates),
				Arguments.of(MultiCandidatesTestClass.class, type, "mySetter", new Method(Setter.class, "getParameterClass"), candidates),
				Arguments.of(MultiCandidatesTestClass.class, type, "mySetter", new Method(Setter.class, "getMutability"), candidates),
				Arguments.of(MultiCandidatesTestClass.class, type, "mySetter", new Method(Setter.class, "getScope"), candidates),
				Arguments.of(MultiCandidatesTestClass.class, type, "mySetter", new Method(Setter.class, "getVisibility"), candidates),
				Arguments.of(MultiCandidatesTestClass.class, type, "mySetter", new Method(Setter.class, "setTo", pojo, (byte) 123), candidates))
				.flatMap(replaceArg(2, arg -> createNamers((String) arg)));
	}

	@ParameterizedTest(name = "{3}, {2}")
	@MethodSource
	void testMethodFailsIfSeveralCandidates(Class<?> pojoClass, Type<?> type, Namer namer, Method method,
			List<String> candidates) {
		Setter<?> setter = new Setter<>(pojoClass, type, namer);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> method.invoke(setter));
		assertEquals("More than one setter found, consider setting a class for " + type + ": " + candidates, exception.getMessage());
	}

	private static Stream<TestNamer> createNamers(String methodName) {
		return createNamers(methodName, new Factory().type().from(byte.class));
	}

	private static Stream<TestNamer> createNamers(String methodName, Type<?> type) {
		TestNamer nameNamer = new TestNamer(methodName);
		TestNamer predicateNamer = new TestNamer(methodName + "(" + type + ")", name -> methodName.equals(name));
		return Stream.of(nameNamer, predicateNamer);
	}

	private static Stream<Type<?>> createTypes(String typeName) {
		return createTypes(typeName, byte.class);
	}

	private static Stream<Type<?>> createTypes(String typeName, Class<?> typeClass) {
		TypeFactory type = new Factory().type();
		Type<?> classType = type.from(typeClass);
		Type<?> namedType = type.as(typeName + "[]");
		Type<?> constrainedNamedType = type.as(typeName + "[" + typeClass + "]", typeClass);
		return Stream.of(classType, constrainedNamedType, namedType);
	}
}
