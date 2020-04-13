package fr.vergne.pester.factory;

import fr.vergne.pester.factory.TypeFactory.Extension;
import fr.vergne.pester.value.Type;

public class Factory {

	private final TypeFactory typeFactory;
	private final GeneratorFactory generatorFactory;
	private final ModifierFactory modifierFactory;

	public Factory() {
		this.typeFactory = new TypeFactory();
		this.generatorFactory = new GeneratorFactory(typeFactory);
		this.modifierFactory = new ModifierFactory(typeFactory, generatorFactory);

		addBasicClasses(typeFactory, generatorFactory, modifierFactory);
		typeFactory.addExtension(createArraysExtension(generatorFactory, modifierFactory));
	}

	private void addBasicClasses(TypeFactory type, GeneratorFactory g, ModifierFactory m) {
		type.from(byte.class).withGenerator(g.ofBytes()).withModifier(m.ofBytes());
		type.from(Byte.class).withGenerator(g.ofBytes()).withModifier(m.ofBytes());
		type.from(short.class).withGenerator(g.ofShorts()).withModifier(m.ofShorts());
		type.from(Short.class).withGenerator(g.ofShorts()).withModifier(m.ofShorts());
		type.from(int.class).withGenerator(g.ofIntegers()).withModifier(m.ofIntegers());
		type.from(Integer.class).withGenerator(g.ofIntegers()).withModifier(m.ofIntegers());
		type.from(long.class).withGenerator(g.ofLongs()).withModifier(m.ofLongs());
		type.from(Long.class).withGenerator(g.ofLongs()).withModifier(m.ofLongs());
		type.from(float.class).withGenerator(g.ofFloats()).withModifier(m.ofFloats());
		type.from(Float.class).withGenerator(g.ofFloats()).withModifier(m.ofFloats());
		type.from(double.class).withGenerator(g.ofDoubles()).withModifier(m.ofDoubles());
		type.from(Double.class).withGenerator(g.ofDoubles()).withModifier(m.ofDoubles());
		type.from(boolean.class).withGenerator(g.ofBooleans()).withModifier(m.ofBooleans());
		type.from(Boolean.class).withGenerator(g.ofBooleans()).withModifier(m.ofBooleans());
		type.from(char.class).withGenerator(g.ofCharacters()).withModifier(m.ofCharacters());
		type.from(Character.class).withGenerator(g.ofCharacters()).withModifier(m.ofCharacters());
		type.from(String.class).withGenerator(g.ofStrings()).withModifier(m.ofStrings());
		type.from(Object.class).withGenerator(g.ofObjects()).withModifier(m.ofObjects());
	}

	private Extension createArraysExtension(GeneratorFactory generator, ModifierFactory modifier) {
		return new Extension() {

			@Override
			public <T> void extendType(Class<T> typeClass, Type<T> classType) {
				if (typeClass.isArray()) {
					Class<?> itemClass = typeClass.getComponentType();
					classType.withGenerator(generator.ofArray(typeClass, itemClass));
					classType.withModifier(modifier.ofArray(typeClass, itemClass));
				}
			}
		};
	}

	public TypeFactory type() {
		return typeFactory;
	}

	public GeneratorFactory generator() {
		return generatorFactory;
	}

	public ModifierFactory modifier() {
		return modifierFactory;
	}
}
