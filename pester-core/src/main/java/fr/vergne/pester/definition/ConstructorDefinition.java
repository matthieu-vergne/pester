package fr.vergne.pester.definition;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fr.vergne.pester.model.Constructor;
import fr.vergne.pester.options.Visibility;
import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Modifier;
import fr.vergne.pester.value.Type;

public class ConstructorDefinition<P> implements InstanciableDefinition<Constructor<P>> {
	
	private final Constructor<P> instance;
	private final Optional<Visibility> visibility;
	private final List<PropertyDefinition<P, ?>> parametersDefinitions;
	private final Generator<List<?>> parametersGenerator;
	private final Modifier<List<?>> parametersModifier;

	public ConstructorDefinition(Class<P> pojoClass, List<PropertyDefinition<P, ?>> parameters, Optional<Visibility> visibility) {
		this.visibility = visibility;
		this.parametersDefinitions = parameters;
		this.parametersGenerator = () -> parameters.stream()
				.map(PropertyDefinition::getGenerator)
				.map(Generator::create)
				.collect(Collectors.toList());
		this.parametersModifier = values -> IntStream.range(0, values.size())
				.mapToObj(i -> newValue(parameters.get(i), values.get(i)))
				.collect(Collectors.toList());
		
		List<Type<?>> parametersTypes = parameters.stream()
				.map(PropertyDefinition::getType)
				.collect(Collectors.toList());
		this.instance = new Constructor<>(pojoClass, parametersTypes);
	}
	
	@SuppressWarnings("unchecked")
	private static <P, T> T newValue(PropertyDefinition<P, T> property, Object value) {
		return property.getModifier().modify((T) value);
	}

	@Override
	public Constructor<P> getInstance() {
		return instance;
	}

	public Optional<Visibility> getVisibility() {
		return visibility;
	}
	
	public Generator<List<?>> getParametersGenerator() {
		return parametersGenerator;
	}

	public Modifier<List<?>> getParametersModifier() {
		return parametersModifier;
	}

	public List<PropertyDefinition<P, ?>> getParametersDefinitions() {
		return parametersDefinitions;
	}
}
