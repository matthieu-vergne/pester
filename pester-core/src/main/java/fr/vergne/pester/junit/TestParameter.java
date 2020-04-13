package fr.vergne.pester.junit;

import static fr.vergne.pester.options.Mutability.*;
import static java.util.function.Predicate.*;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import fr.vergne.pester.definition.ConstructorDefinition;
import fr.vergne.pester.definition.FieldDefinition;
import fr.vergne.pester.definition.GetterDefinition;
import fr.vergne.pester.definition.InstanciableDefinition;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;
import fr.vergne.pester.definition.SetterDefinition;
import fr.vergne.pester.junit.extension.TestSpecificity;

/**
 * These values are sorted by importance to order the tests through
 * {@link TestSpecificity}.
 */
public enum TestParameter {
	CONSTRUCTOR(onConstructor(InstanciableDefinition::getInstance)),
	CONSTRUCTOR_VISIBILITY(onConstructor(ConstructorDefinition::getVisibility)),
	DEFAULT_CONSTRUCTOR(onConstructor(def -> def.getParametersDefinitions().isEmpty() ? def.getInstance() : null)),
	CONSTRUCTOR_PARAMETER_INDEX(onConstructorProperty((constDef, propDef) -> constDef.getParametersDefinitions().indexOf(propDef))),
	CONSTRUCTOR_FIELD(onConstructorProperty((constDef, propDef) -> propDef.getFieldDefinition().map(InstanciableDefinition::getInstance))),
	CONSTRUCTOR_GETTER(onConstructorProperty((constDef, propDef) -> propDef.getGetterDefinition().map(InstanciableDefinition::getInstance))),
	CONSTRUCTOR_PARAMETERS_GENERATOR(onConstructor(ConstructorDefinition::getParametersGenerator)),
	CONSTRUCTOR_PARAMETERS_MODIFIER(onConstructor(ConstructorDefinition::getParametersModifier)),

	FIELD(onField(InstanciableDefinition::getInstance)),
	MUTABLE_FIELD(onField(def -> def.getMutability().filter(isEqual(NON_FINAL)).map(mutability -> def.getInstance()))),
	FIELD_SCOPE(onField(FieldDefinition::getScope)),
	FIELD_MUTABILITY(onField(FieldDefinition::getMutability)),
	FIELD_VISIBILITY(onField(FieldDefinition::getVisibility)),

	GETTER(onGetter(InstanciableDefinition::getInstance)),
	GETTER_SCOPE(onGetter(GetterDefinition::getScope)),
	GETTER_MUTABILITY(onGetter(GetterDefinition::getMutability)),
	GETTER_VISIBILITY(onGetter(GetterDefinition::getVisibility)),
	
	SETTER(onSetter(InstanciableDefinition::getInstance)),
	SETTER_SCOPE(onSetter(SetterDefinition::getScope)),
	SETTER_MUTABILITY(onSetter(SetterDefinition::getMutability)),
	SETTER_VISIBILITY(onSetter(SetterDefinition::getVisibility)),

	CLASS(onProperty(def -> def.getType().getTypeClass())),
	DEFAULT_VALUE(onProperty(PropertyDefinition::getDefaultValue)),

	POJO_CLASS(onPojo(PojoDefinition::getPojoClass)),
	POJO_PARENT_CLASS(DefinitionItem::getParentClass),
	POJO_INTERFACE(DefinitionItem::getInterfaceClass),
	POJO_GENERATOR(onPojo(PojoDefinition::getPojoGenerator)),
	VALUE_GENERATOR(onProperty(PropertyDefinition::getGenerator)),
	VALUE_MODIFIER(onProperty(PropertyDefinition::getModifier));

	private final Function<DefinitionItem<?>, ?> extractor;

	private <T> TestParameter(Function<DefinitionItem<?>, ?> extractor) {
		this.extractor = extractor;
	}

	Object extractFrom(DefinitionItem<?> data) {
		return extractor.apply(data);
	}

	private static Function<DefinitionItem<?>, ?> onPojo(Function<PojoDefinition<?>, Object> extractor) {
		return definitionData -> definitionData.getPojo().map(extractor).orElse(Optional.empty());
	}

	private static Function<DefinitionItem<?>, ?> onConstructor(Function<ConstructorDefinition<?>, Object> extractor) {
		return definitionData -> definitionData.getConstructor().map(extractor).orElse(Optional.empty());
	}

	private static Function<DefinitionItem<?>, ?> onProperty(Function<PropertyDefinition<?, ?>, Object> extractor) {
		return definitionData -> definitionData.getProperty().map(extractor).orElse(Optional.empty());
	}

	private static Function<DefinitionItem<?>, ?> onField(Function<FieldDefinition<?, ?>, Object> extractor) {
		return definitionData -> definitionData
				.getProperty()
				.map(def -> def.getFieldDefinition().orElse(null))
				.map(extractor)
				.orElse(Optional.empty());
	}

	private static Function<DefinitionItem<?>, ?> onGetter(Function<GetterDefinition<?, ?>, Object> extractor) {
		return definitionData -> definitionData
				.getProperty()
				.map(def -> def.getGetterDefinition().orElse(null))
				.map(extractor)
				.orElse(Optional.empty());
	}

	private static Function<DefinitionItem<?>, ?> onSetter(Function<SetterDefinition<?, ?>, Object> extractor) {
		return definitionData -> definitionData
				.getProperty()
				.map(def -> def.getSetterDefinition().orElse(null))
				.map(extractor)
				.orElse(Optional.empty());
	}

	private static Function<DefinitionItem<?>, ?> onConstructorProperty(
			BiFunction<ConstructorDefinition<?>, PropertyDefinition<?, ?>, ?> extractor) {
		return definitionData -> {
			Optional<? extends ConstructorDefinition<?>> constructorOpt = definitionData.getConstructor();
			Optional<? extends PropertyDefinition<?, ?>> propertyOpt = definitionData.getProperty();
			if (constructorOpt.isPresent() && propertyOpt.isPresent()) {
				ConstructorDefinition<?> constructor = constructorOpt.get();
				PropertyDefinition<?, ?> property = propertyOpt.get();
				if (constructor.getParametersDefinitions().contains(property)) {
					return extractor.apply(constructor, property);
				}
			}
			return Optional.empty();
		};
	}
}
