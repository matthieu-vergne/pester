package fr.vergne.pester.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;

import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.testutil.Method;

@SuppressWarnings("unused")
class FieldTest {

	static class TestClass {
		byte myField;

		@Override
		public String toString() {
			return "pojo";// Improve test names
		}
	}

	@ParameterizedTest
	@NullSource
	void testConstructorFailsWhenNoPojoClass(Class<?> pojoClass) {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Field<>(pojoClass, "someField"));
		assertEquals("No POJO class provided", exception.getMessage());
	}

	@ParameterizedTest
	@NullAndEmptySource
	void testConstructorFailsWhenNoName(String name) {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new Field<>(TestClass.class, name));
		assertEquals("No field name provided", exception.getMessage());
	}

	@Test
	void testIsPresentReturnsTrueWhenPresent() {
		String name = "myField";
		Field<TestClass> field = new Field<>(TestClass.class, name);
		assertTrue(field.isPresent(), name + " field not found");
	}

	@Test
	void testIsPresentReturnsFalseWhenAbsent() {
		String name = "unknownField";
		Field<TestClass> field = new Field<>(TestClass.class, name);
		assertFalse(field.isPresent(), name + " field found");
	}

	static Stream<Arguments> testMethodFailsDefinitionWhenAbsent() throws NoSuchMethodException, SecurityException {
		TestClass pojo = new TestClass();
		return Stream.of(
				Arguments.of(TestClass.class, new Method(Field.class, "getFieldClass")),
				Arguments.of(TestClass.class, new Method(Field.class, "getMutability")),
				Arguments.of(TestClass.class, new Method(Field.class, "getScope")),
				Arguments.of(TestClass.class, new Method(Field.class, "getVisibility")),
				Arguments.of(TestClass.class, new Method(Field.class, "getFrom", pojo)),
				Arguments.of(TestClass.class, new Method(Field.class, "setTo", pojo, (byte) 123)));
	}

	@ParameterizedTest(name = "{1}")
	@MethodSource
	void testMethodFailsDefinitionWhenAbsent(Class<?> pojoClass, Method method) {
		String fieldName = "fieldThatShouldNotBeDeclaredInTheClass";
		Field<?> field = new Field<>(pojoClass, fieldName);
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> method.invoke(field));
		assertEquals("Field " + fieldName + " not found", exception.getMessage());
	}

	static Stream<Arguments> testGetFieldClassReturnsActualFieldClass() {
		class Custom {
		}
		class ReturnTestClass {
			byte primitiveField;
			Integer objectField;
			Custom customField;
			List<Object> genericField;
		}
		return Stream.of(Arguments.of(ReturnTestClass.class, "primitiveField", byte.class),
				Arguments.of(ReturnTestClass.class, "objectField", Integer.class),
				Arguments.of(ReturnTestClass.class, "customField", Custom.class),
				Arguments.of(ReturnTestClass.class, "genericField", List.class));
	}

	@ParameterizedTest(name = "{1} is a {2}")
	@MethodSource
	void testGetFieldClassReturnsActualFieldClass(Class<?> pojoClass, String fieldName, Class<Byte> expectedClass) {
		Field<?> field = new Field<>(pojoClass, fieldName);
		assertEquals(expectedClass, field.getFieldClass());
	}

	static Stream<Arguments> testGetMutabilityReturnsActualMutability() {
		class MutabilityTestClass {
			byte nonFinalField;
			final byte finalField = 0;
		}
		return Stream.of(Arguments.of(MutabilityTestClass.class, "nonFinalField", Mutability.NON_FINAL),
				Arguments.of(MutabilityTestClass.class, "finalField", Mutability.FINAL));
	}

	@ParameterizedTest(name = "{1} is {2}")
	@MethodSource
	void testGetMutabilityReturnsActualMutability(Class<?> pojoClass, String fieldName,
			Mutability expectedMutability) {
		Field<?> field = new Field<>(pojoClass, fieldName);
		assertEquals(expectedMutability, field.getMutability());
	}

	static class ScopeTestClass {
		byte nonStaticField;
		static byte staticField;
	}

	static Stream<Arguments> testGetScopeReturnsActualScope() {
		return Stream.of(Arguments.of(ScopeTestClass.class, "nonStaticField", Scope.NON_STATIC),
				Arguments.of(ScopeTestClass.class, "staticField", Scope.STATIC));
	}

	@ParameterizedTest(name = "{1} is {2}")
	@MethodSource
	void testGetScopeReturnsActualScope(Class<?> pojoClass, String fieldName, Scope expectedScope) {
		Field<?> field = new Field<>(pojoClass, fieldName);
		assertEquals(expectedScope, field.getScope());
	}

	static Stream<Arguments> testGetVisibilityReturnsActualVisibility() {
		class VisibilityTestClass {
			private byte privateField;
			protected byte protectedField;
			byte packageField;
			public byte publicField;
		}
		return Stream.of(Arguments.of(VisibilityTestClass.class, "privateField", Visibility.PRIVATE),
				Arguments.of(VisibilityTestClass.class, "protectedField", Visibility.PROTECTED),
				Arguments.of(VisibilityTestClass.class, "packageField", Visibility.PACKAGE),
				Arguments.of(VisibilityTestClass.class, "publicField", Visibility.PUBLIC));
	}

	@ParameterizedTest(name = "{1} is {2}")
	@MethodSource
	void testGetVisibilityReturnsActualVisibility(Class<?> pojoClass, String fieldName,
			Visibility expectedVisibility) {
		Field<?> field = new Field<>(pojoClass, fieldName);
		assertEquals(expectedVisibility, field.getVisibility());
	}

	@Test
	void testGetFromReturnsCurrentValue() {
		TestClass pojo = new TestClass();
		byte expectedValue = 123;

		Field<TestClass> field = new Field<>(TestClass.class, "myField");
		pojo.myField = expectedValue;
		Object actualValue = field.getFrom(pojo);
		assertEquals(expectedValue, actualValue);
	}

	@Test
	void testSetToFailsDefinitionOnInvalidClass() {
		TestClass pojo = new TestClass();

		Field<TestClass> field = new Field<>(TestClass.class, "myField");
		Exception exception = assertThrows(DefinitionUnfulfilledException.class, () -> field.setTo(pojo, new Object()));
		assertEquals("Field myField only accepts byte values", exception.getMessage());
	}

	@Test
	void testSetToSetsCurrentValue() {
		TestClass pojo = new TestClass();
		byte expectedValue = 123;

		Field<TestClass> field = new Field<>(TestClass.class, "myField");
		field.setTo(pojo, expectedValue);
		byte actualValue = pojo.myField;
		assertEquals(expectedValue, actualValue);
	}

	@Test
	void testToStringReturnsFieldName() {
		String fieldName = "myField";
		Field<TestClass> field = new Field<>(TestClass.class, fieldName);
		assertEquals(fieldName, field.toString());
	}
}
