package fr.vergne.pester.model;

import static fr.vergne.pester.testutil.ArgumentsSupport.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.testutil.Method;
import fr.vergne.pester.util.namer.Namer;

@SuppressWarnings("unused")
class GetterTest {

	static class TestClass {
		byte getterValue;

		byte myGetter() {
			return getterValue;
		}

		byte myGetter(byte arg) {
			throw new RuntimeException("this method should never be used: no arg criteria fails");
		}

		byte myOtherGetter() {
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
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Getter<>(pojoClass, new TestNamer("getter")));
		assertEquals("No POJO class provided", exception.getMessage());
	}

	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoNamer(Namer namer) {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Getter<>(TestClass.class, namer));
		assertEquals("No namer provided", exception.getMessage());
	}
	
	static Stream<Arguments> testIsPresentReturnsTrueWhenPresent() {
		return Stream.of(Arguments.of("myGetter()")).flatMap(appendArg(() -> createNamers("myGetter")));
	}

	@ParameterizedTest
	@MethodSource
	void testIsPresentReturnsTrueWhenPresent(String signature, Namer namer) {
		Getter<TestClass> getter = new Getter<>(TestClass.class, namer);
		assertTrue(getter.isPresent(), signature + " not found");
	}

	static Stream<Arguments> testIsPresentReturnsFalseWhenAbsent() {
		return Stream.of(Arguments.of("unknownGetter()")).flatMap(appendArg(() -> createNamers("unknownGetter")));
	}

	@ParameterizedTest
	@MethodSource
	void testIsPresentReturnsFalseWhenAbsent(String signature, Namer namer) {
		Getter<TestClass> getter = new Getter<>(TestClass.class, namer);
		assertFalse(getter.isPresent(), signature + " found");
	}

	static Stream<Arguments> testGetReturnClassReturnsActualReturnClass() {
		class Custom {
		}
		class ReturnTestClass {
			byte primitiveGetter() {return 0;}
			Integer objectGetter() {return 0;}
			Custom customGetter() {return new Custom();}
			List<Object> genericGetter() {return Collections.emptyList();}
		}
		
		return Stream.of(Arguments.of(ReturnTestClass.class, "primitiveGetter", byte.class),
				Arguments.of(ReturnTestClass.class, "objectGetter", Integer.class),
				Arguments.of(ReturnTestClass.class, "customGetter", Custom.class),
				Arguments.of(ReturnTestClass.class, "genericGetter", List.class))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)));
	}

	@ParameterizedTest(name = "{1} returns a {2}")
	@MethodSource
	void testGetReturnClassReturnsActualReturnClass(Class<?> pojoClass, Namer namer, Class<Byte> expectedClass) {
		Getter<?> getter = new Getter<>(pojoClass, namer);
		assertEquals(expectedClass, getter.getReturnClass());
	}

	static Stream<Arguments> testGetMutabilityReturnsActualMutability() {
		class MutabilityTestClass {
			byte nonFinalGetter() {return 0;}
			final byte finalGetter() {return 0;}
		}
		return Stream.of(Arguments.of(MutabilityTestClass.class, "nonFinalGetter", Mutability.NON_FINAL),
				Arguments.of(MutabilityTestClass.class, "finalGetter", Mutability.FINAL))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)));
	}

	@ParameterizedTest(name = "{1} is {2}")
	@MethodSource
	void testGetMutabilityReturnsActualMutability(Class<?> pojoClass, Namer namer, Mutability expectedMutability) {
		Getter<?> getter = new Getter<>(pojoClass, namer);
		assertEquals(expectedMutability, getter.getMutability());
	}

	static class ScopeTestClass {
		byte nonStaticGetter() {return 0;}
		static byte staticGetter() {return 0;}
	}

	static Stream<Arguments> testGetScopeReturnsActualScope() {
		return Stream.of(Arguments.of(ScopeTestClass.class, "nonStaticGetter", Scope.NON_STATIC),
				Arguments.of(ScopeTestClass.class, "staticGetter", Scope.STATIC))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)));
	}

	@ParameterizedTest(name = "{1} is {2}")
	@MethodSource
	void testGetScopeReturnsActualScope(Class<?> pojoClass, Namer namer, Scope expectedScope) {
		Getter<?> getter = new Getter<>(pojoClass, namer);
		assertEquals(expectedScope, getter.getScope());
	}

	static Stream<Arguments> testGetVisibilityReturnsActualVisibility() {
		class VisibilityTestClass {
			private byte privateGetter() {return 0;}
			protected byte protectedGetter() {return 0;}
			byte packageGetter() {return 0;}
			public byte publicGetter() {return 0;}
		}
		return Stream.of(Arguments.of(VisibilityTestClass.class, "privateGetter", Visibility.PRIVATE),
				Arguments.of(VisibilityTestClass.class, "protectedGetter", Visibility.PROTECTED),
				Arguments.of(VisibilityTestClass.class, "packageGetter", Visibility.PACKAGE),
				Arguments.of(VisibilityTestClass.class, "publicGetter", Visibility.PUBLIC))
				.flatMap(replaceArg(1, arg -> createNamers((String) arg)));
	}

	@ParameterizedTest(name = "{1} is {2}")
	@MethodSource
	void testGetVisibilityReturnsActualVisibility(Class<?> pojoClass, Namer namer, Visibility expectedVisibility) {
		Getter<?> getter = new Getter<>(pojoClass, namer);
		assertEquals(expectedVisibility, getter.getVisibility());
	}

	static Stream<TestNamer> testGetFromCallsGetter() {
		return createNamers("myGetter");
	}

	@ParameterizedTest
	@MethodSource
	void testGetFromCallsGetter(Namer namer) {
		TestClass pojo = new TestClass();
		byte expectedValue = 123;

		Getter<TestClass> getter = new Getter<>(TestClass.class, namer);
		pojo.getterValue = expectedValue;
		Object actualValue = getter.getFrom(pojo);
		assertEquals(expectedValue, actualValue);
	}

	static Stream<TestNamer> testGetFromFailsDefinitionOnException() {
		return createNamers("myGetter");
	}

	@ParameterizedTest
	@MethodSource
	void testGetFromFailsDefinitionOnException(Namer namer) {
		@SuppressWarnings("serial")
		class TestException extends RuntimeException {
		}
		TestException testException = new TestException();
		class ExceptionTestClass {
			byte myGetter() {throw testException;}
		}
		ExceptionTestClass pojo = new ExceptionTestClass();

		Getter<ExceptionTestClass> getter = new Getter<>(ExceptionTestClass.class, namer);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> getter.getFrom(pojo));
		assertEquals("myGetter() throws an exception", exception.getMessage());
		assertEquals(testException, exception.getCause());
	}

	static Stream<TestNamer> testToStringReturnsGetterSignature() {
		return createNamers("myGetter");
	}

	@ParameterizedTest
	@MethodSource
	void testToStringReturnsGetterSignature(Namer namer) {
		Getter<TestClass> getter = new Getter<>(TestClass.class, namer);
		assertEquals("myGetter()", getter.toString());
	}

	static Stream<Arguments> testMethodFailsDefinitionWhenAbsent() throws NoSuchMethodException, SecurityException {
		TestClass pojo = new TestClass();
		return Stream.of(
				Arguments.of(TestClass.class, new Method(Getter.class, "getReturnClass")),
				Arguments.of(TestClass.class, new Method(Getter.class, "getMutability")),
				Arguments.of(TestClass.class, new Method(Getter.class, "getScope")),
				Arguments.of(TestClass.class, new Method(Getter.class, "getVisibility")),
				Arguments.of(TestClass.class, new Method(Getter.class, "getFrom", pojo)))
				.flatMap(appendArg(() -> createNamers("getterThatShouldNotBeDeclaredInTheClass")))
				.flatMap(appendArg(() -> Stream.of("getterThatShouldNotBeDeclaredInTheClass()")));
	}

	@ParameterizedTest(name = "{1}, {2}")
	@MethodSource
	void testMethodFailsDefinitionWhenAbsent(Class<?> pojoClass, Method method, Namer namer, String signature) {
		Getter<?> getter = new Getter<>(pojoClass, namer);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> method.invoke(getter));
		assertEquals(signature + " not found", exception.getMessage());
	}

	private static Stream<TestNamer> createNamers(String methodName) {
		TestNamer nameNamer = new TestNamer(methodName);
		TestNamer predicateNamer = new TestNamer(methodName + "()", name -> methodName.equals(name));
		return Stream.of(nameNamer, predicateNamer);
	}
}
