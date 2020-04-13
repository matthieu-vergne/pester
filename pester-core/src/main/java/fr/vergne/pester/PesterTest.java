package fr.vergne.pester;

import static fr.vergne.pester.PesterTestUtil.*;
import static fr.vergne.pester.junit.TestParameter.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;

import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.junit.annotation.DefinitionSource;
import fr.vergne.pester.junit.annotation.DisableParameterizedTestsWithNoCase;
import fr.vergne.pester.junit.annotation.TestTarget;
import fr.vergne.pester.junit.extension.DefinitionSourcerExtension;
import fr.vergne.pester.junit.extension.TestSpecificity;
import fr.vergne.pester.model.Constructor;
import fr.vergne.pester.model.Field;
import fr.vergne.pester.model.Getter;
import fr.vergne.pester.model.Setter;
import fr.vergne.pester.options.Mutability;
import fr.vergne.pester.options.Scope;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Modifier;

@TestInstance(Lifecycle.PER_CLASS)
@DisableParameterizedTestsWithNoCase
@TestMethodOrder(TestSpecificity.class)
@ExtendWith(DefinitionSourcerExtension.class)
public interface PesterTest<P> {

	public PojoDefinition<P> createPojoDefinition();

	@ParameterizedTest(name = "{0} is present")
	@TestTarget(FIELD)
	@DefinitionSource({ FIELD })
	default void testFieldIsPresent(Field<P> field) {
		assertTrue(field.isPresent(), field + " not found");
	}

	@ParameterizedTest(name = "{0} is a {1} field")
	@TestTarget(FIELD)
	@DefinitionSource({ FIELD, CLASS })
	default void testFieldType(Field<P> field, Class<?> expectedClass) {
		assertEquals(expectedClass, field.getFieldClass());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(FIELD)
	@DefinitionSource({ FIELD, FIELD_VISIBILITY })
	default void testFieldVisibility(Field<P> field, Visibility expectedVisibility) {
		assertEquals(expectedVisibility, field.getVisibility());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(FIELD)
	@DefinitionSource({ FIELD, FIELD_MUTABILITY })
	default void testFieldMutability(Field<P> field, Mutability expectedMutability) {
		assertEquals(expectedMutability, field.getMutability());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(FIELD)
	@DefinitionSource({ FIELD, FIELD_SCOPE })
	default void testFieldScope(Field<P> field, Scope expectedScope) {
		assertEquals(expectedScope, field.getScope());
	}

	@ParameterizedTest(name = "{0} is present")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER })
	default void testGetterIsPresent(Getter<P> getter) {
		assertTrue(getter.isPresent(), getter + " not found");
	}

	@ParameterizedTest(name = "{0} returns a {1}")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, CLASS })
	default void testGetterType(Getter<P> getter, Class<?> expectedClass) {
		assertEquals(expectedClass, getter.getReturnClass());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, GETTER_VISIBILITY })
	default void testGetterVisibility(Getter<P> getter, Visibility expectedVisibility) {
		assertEquals(expectedVisibility, getter.getVisibility());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, GETTER_MUTABILITY })
	default void testGetterMutability(Getter<P> getter, Mutability expectedMutability) {
		assertEquals(expectedMutability, getter.getMutability());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, GETTER_SCOPE })
	default void testGetterScope(Getter<P> getter, Scope expectedScope) {
		assertEquals(expectedScope, getter.getScope());
	}

	@ParameterizedTest(name = "{0} does not throw upon call")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, POJO_GENERATOR })
	default void testGetterDoesNotThrow(Getter<P> getter, Generator<P> pojoGenerator) {
		// GIVEN
		P pojo = pojoGenerator.create();

		// WHEN
		Executable call = () -> getter.getFrom(pojo);

		// THEN
		assertDoesNotThrow(call);
	}

	@ParameterizedTest(name = "{0} is present")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER })
	default void testSetterIsPresent(Setter<P> setter) {
		assertTrue(setter.isPresent(), setter + " not found");
	}

	@ParameterizedTest(name = "{0} consumes a {1}")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER, CLASS })
	default void testSetterType(Setter<P> setter, Class<?> expectedClass) {
		assertEquals(expectedClass, setter.getParameterClass());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER, SETTER_VISIBILITY })
	default void testSetterVisibility(Setter<P> setter, Visibility expectedVisibility) {
		assertEquals(expectedVisibility, setter.getVisibility());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER, SETTER_MUTABILITY })
	default void testSetterMutability(Setter<P> setter, Mutability expectedMutability) {
		assertEquals(expectedMutability, setter.getMutability());
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER, SETTER_SCOPE })
	default void testSetterScope(Setter<P> setter, Scope expectedScope) {
		assertEquals(expectedScope, setter.getScope());
	}

	@ParameterizedTest(name = "{0} does not throw upon call")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER, POJO_GENERATOR, VALUE_GENERATOR })
	default void testSetterDoesNotThrow(Setter<P> setter, Generator<P> pojoGenerator, Generator<?> valueGenerator) {
		// GIVEN
		P pojo = pojoGenerator.create();
		Object value = valueGenerator.create();

		// WHEN
		Executable call = () -> setter.setTo(pojo, value);

		// THEN
		assertDoesNotThrow(call);
	}

	@ParameterizedTest(name = "{0} reads from {1}")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, MUTABLE_FIELD, POJO_GENERATOR, VALUE_GENERATOR, VALUE_MODIFIER })
	default <T> void testGetterReadsFromField(Getter<P> getter, Field<P> field, Generator<P> pojoGenerator,
			Generator<T> valueGenerator, Modifier<T> valueModifier) {
		// GIVEN
		P pojo = pojoGenerator.create();
		T expectedValue = valueGenerator.create();

		// WHEN
		field.setTo(pojo, expectedValue);
		Object actualValue = getter.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);

		// try again, just in case the initial value was already there

		// GIVEN
		T expectedValue2 = valueModifier.modify(expectedValue);
		assertNotEquals(expectedValue, expectedValue2);

		// WHEN
		field.setTo(pojo, expectedValue2);
		Object actualValue2 = getter.getFrom(pojo);

		// THEN
		assertEquals(expectedValue2, actualValue2);
	}

	@ParameterizedTest(name = "{0} stores in {1}")
	@TestTarget(SETTER)
	@DefinitionSource({ SETTER, FIELD, POJO_GENERATOR, VALUE_GENERATOR, VALUE_MODIFIER })
	default <T> void testSetterStoresInField(Setter<P> setter, Field<P> field, Generator<P> pojoGenerator,
			Generator<T> valueGenerator, Modifier<T> valueModifier) {
		// GIVEN
		P pojo = pojoGenerator.create();
		T expectedValue = valueGenerator.create();

		// WHEN
		setter.setTo(pojo, expectedValue);
		Object actualValue = field.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);

		// try again, just in case the initial value was already there

		// GIVEN
		T expectedValue2 = valueModifier.modify(expectedValue);
		assertNotEquals(expectedValue, expectedValue2);

		// WHEN
		setter.setTo(pojo, expectedValue2);
		Object actualValue2 = field.getFrom(pojo);

		// THEN
		assertEquals(expectedValue2, actualValue2);
	}

	@ParameterizedTest(name = "{0} returns value given to {1}")
	@TestTarget(GETTER)
	@DefinitionSource({ GETTER, SETTER, POJO_GENERATOR, VALUE_GENERATOR, VALUE_MODIFIER })
	default <T> void testGetterReturnsSetValue(Getter<P> getter, Setter<P> setter, Generator<P> pojoGenerator,
			Generator<T> valueGenerator, Modifier<T> valueModifier) {
		// GIVEN
		P pojo = pojoGenerator.create();
		T expectedValue = valueGenerator.create();

		// WHEN
		setter.setTo(pojo, expectedValue);
		Object actualValue = getter.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);

		// try again, just in case the initial value was already there

		// GIVEN
		T expectedValue2 = valueModifier.modify(expectedValue);
		assertNotEquals(expectedValue, expectedValue2);

		// WHEN
		setter.setTo(pojo, expectedValue2);
		Object actualValue2 = getter.getFrom(pojo);

		// THEN
		assertEquals(expectedValue2, actualValue2);
	}

	@ParameterizedTest(name = "{0} is present")
	@TestTarget(CONSTRUCTOR)
	@DefinitionSource({ CONSTRUCTOR })
	default void testConstructorIsPresent(Constructor<P> constructor) {
		assertTrue(constructor.isPresent(), constructor + " not found");
	}

	@ParameterizedTest(name = "{0} is {1}")
	@TestTarget(CONSTRUCTOR)
	@DefinitionSource({ CONSTRUCTOR, CONSTRUCTOR_VISIBILITY })
	default void testConstructorVisibility(Constructor<P> constructor, Visibility expectedVisibility) {
		assertEquals(expectedVisibility, constructor.getVisibility());
	}

	@ParameterizedTest(name = "{0} does not throw")
	@TestTarget(CONSTRUCTOR)
	@DefinitionSource({ CONSTRUCTOR, CONSTRUCTOR_PARAMETERS_GENERATOR })
	default void testConstructorDoesNotThrow(Constructor<P> constructor, Generator<List<?>> parametersGenerator) {
		// GIVEN
		List<?> parameters = parametersGenerator.create();

		// WHEN
		Executable executable = () -> constructor.invoke(parameters);

		// THEN
		assertDoesNotThrow(executable);
	}

	@ParameterizedTest(name = "{0} set field {1} with parameter {2}")
	@TestTarget(CONSTRUCTOR)
	@DefinitionSource({ CONSTRUCTOR, CONSTRUCTOR_FIELD, CONSTRUCTOR_PARAMETER_INDEX, CONSTRUCTOR_PARAMETERS_GENERATOR,
			CONSTRUCTOR_PARAMETERS_MODIFIER })
	default void testConstructorSetField(Constructor<P> constructor, Field<P> field, int parameterIndex,
			Generator<List<?>> parametersGenerator, Modifier<List<?>> parametersModifier) {
		// GIVEN
		List<?> parameters = parametersGenerator.create();
		Object expectedValue = parameters.get(parameterIndex);

		// WHEN
		P pojo = constructor.invoke(parameters);
		Object actualValue = field.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);

		// try again, just in case the initial value was already there

		// GIVEN
		List<?> parameters2 = parametersModifier.modify(parameters);
		Object expectedValue2 = parameters2.get(parameterIndex);
		assertNotEquals(expectedValue, expectedValue2);

		// WHEN
		P pojo2 = constructor.invoke(parameters2);
		Object actualValue2 = field.getFrom(pojo2);

		// THEN
		assertEquals(expectedValue2, actualValue2);
	}

	@ParameterizedTest(name = "{0} set getter value of {1} with parameter {2}")
	@TestTarget(CONSTRUCTOR)
	@DefinitionSource({ CONSTRUCTOR, CONSTRUCTOR_GETTER, CONSTRUCTOR_PARAMETER_INDEX, CONSTRUCTOR_PARAMETERS_GENERATOR,
			CONSTRUCTOR_PARAMETERS_MODIFIER })
	default void testConstructorSetGetterValue(Constructor<P> constructor, Getter<P> getter, int parameterIndex,
			Generator<List<?>> parametersGenerator, Modifier<List<?>> parametersModifier) {
		// GIVEN
		List<?> parameters = parametersGenerator.create();
		Object expectedValue = parameters.get(parameterIndex);

		// WHEN
		P pojo = constructor.invoke(parameters);
		Object actualValue = getter.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);

		// try again, just in case the initial value was already there

		// GIVEN
		List<?> parameters2 = parametersModifier.modify(parameters);
		Object expectedValue2 = parameters2.get(parameterIndex);
		assertNotEquals(expectedValue, expectedValue2);

		// WHEN
		P pojo2 = constructor.invoke(parameters2);
		Object actualValue2 = getter.getFrom(pojo2);

		// THEN
		assertEquals(expectedValue2, actualValue2);
	}

	@ParameterizedTest(name = "{0} assigns \"{1}\" to {2}")
	@TestTarget(DEFAULT_CONSTRUCTOR)
	@DefinitionSource({ DEFAULT_CONSTRUCTOR, DEFAULT_VALUE, FIELD })
	default void testDefaultConstructorAssignsFieldWithDefaultValue(Constructor<P> constructor, Object expectedValue,
			Field<P> field) {
		// GIVEN
		P pojo = constructor.invoke(noParameter());

		// WHEN
		Object actualValue = field.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);
	}

	@ParameterizedTest(name = "{0} assigns \"{1}\" to {2}")
	@TestTarget(DEFAULT_CONSTRUCTOR)
	@DefinitionSource({ DEFAULT_CONSTRUCTOR, DEFAULT_VALUE, GETTER })
	default void testDefaultConstructorAssignsGetterValueWithDefaultValue(Constructor<P> constructor,
			Object expectedValue, Getter<P> getter) {
		// GIVEN
		P pojo = constructor.invoke(noParameter());

		// WHEN
		Object actualValue = getter.getFrom(pojo);

		// THEN
		assertEquals(expectedValue, actualValue);
	}

	@ParameterizedTest(name = "{0} extends {1}")
	@TestTarget(POJO_CLASS)
	@DefinitionSource({ POJO_CLASS, POJO_PARENT_CLASS })
	default void testPojoClassExtendsParentClass(Class<P> pojoClass, Class<?> parentClass) {
		assertTrue(parentClass.isAssignableFrom(pojoClass), pojoClass + " does not implement " + parentClass);
	}

	@ParameterizedTest(name = "{0} implements {1}")
	@TestTarget(POJO_CLASS)
	@DefinitionSource({ POJO_CLASS, POJO_INTERFACE })
	default void testPojoClassImplementsInterface(Class<P> pojoClass, Class<?> interfaceClass) {
		assertTrue(interfaceClass.isAssignableFrom(pojoClass), pojoClass + " does not implement " + interfaceClass);
	}
}
