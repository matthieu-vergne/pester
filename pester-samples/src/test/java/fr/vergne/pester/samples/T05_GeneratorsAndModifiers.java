package fr.vergne.pester.samples;

import java.util.stream.Stream;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;
import fr.vergne.pester.samples.T05_GeneratorsAndModifiers.Pojo;
import fr.vergne.pester.value.Generator;
import fr.vergne.pester.value.Modifier;
import fr.vergne.pester.value.Type;

class T05_GeneratorsAndModifiers implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo::new);
		
		// A generator aims at providing a value when called
		Generator<Integer> intGenerator = () -> 10;
		int value = intGenerator.create(); // 10
		System.out.println("Generator creates " + value);

		// A modifier aims at providing a value different from the received one
		Modifier<Integer> intModifier = i -> i + 2;
		int newValue = intModifier.modify(3); // 5
		System.out.println("Modifier replaces 3 by " + newValue);
		
		/*
		 * Generators and modifiers are combined to generate as many different values as
		 * we need, so it is not necessary to care about generating different values in
		 * the generator. However, it might be useful to take care of the null value in
		 * the modifier.
		 */

		// Generators and modifiers are usually assigned to types
		Type<Integer> type = def.create().type().as("type", int.class);
		type.withGenerator(intGenerator);
		type.withModifier(intModifier);
		System.out.println("Type generator creates " + type.getGenerator().create());
		System.out.println("Type modifier replaces 3 by " + type.getModifier().modify(3));		

		// Among class-based types, basic ones already have default generators and modifiers
		Type<Integer> classType = def.create().type().from(int.class);
		System.out.println("Int generator creates " + classType.getGenerator().create());
		System.out.println("Int modifier replaces 3 by " + classType.getModifier().modify(3));		

		// Since all class-based types are the same, they share their generator and modifier
		Type<Integer> classType1 = def.create().type().from(int.class);
		classType1.withGenerator(() -> 42);
		Type<Integer> classType2 = def.create().type().from(int.class);
		classType2.withModifier(i -> i - 1);
		boolean hasSameGenerator = classType1.getGenerator().equals(classType2.getGenerator()); // true
		boolean hasSameModifier = classType1.getModifier().equals(classType2.getModifier()); // true
		System.out.println("Class-based types have same generator? " + hasSameGenerator);
		System.out.println("Class-based types have same modifier? " + hasSameModifier);
		
		// A property defined on a type uses its generator and modifier by default
		PropertyDefinition<Pojo, Integer> typeProperty = def
				.addProperty(type, "typeProperty");
		boolean hasSameGeneratorType = type.getGenerator().equals(typeProperty.getGenerator()); // true
		boolean hasSameModifierType = type.getModifier().equals(typeProperty.getModifier()); // true
		System.out.println("Type and property have same generator? " + hasSameGeneratorType);
		System.out.println("Type and property have same modifier? " + hasSameModifierType);

		// A property defined on a class uses the class-based type
		PropertyDefinition<Pojo, Integer> classProperty = def
				.addProperty(int.class, "classProperty");
		boolean hasSameGeneratorClass = def.create().type().from(int.class).getGenerator().equals(classProperty.getGenerator()); // true
		boolean hasSameModifierClass = def.create().type().from(int.class).getModifier().equals(classProperty.getModifier()); // true
		System.out.println("Class and property have same generator? " + hasSameGeneratorClass);
		System.out.println("Class and property have same modifier? " + hasSameModifierClass);
		
		// Each property can also have a dedicated generator or modifier
		PropertyDefinition<Pojo, Integer> customProperty = def
				.addProperty(int.class, "customProperty")
				.withGenerator(() -> 5)
				.withModifier(i -> i + 5);
		boolean hasDefaultGenerator = def.create().type().from(int.class).getGenerator().equals(customProperty.getGenerator()); // false
		boolean hasDefaultModifier = def.create().type().from(int.class).getModifier().equals(customProperty.getModifier()); // false
		System.out.println("Customized property has default generator? " + hasDefaultGenerator);
		System.out.println("Customized property has default modifier? " + hasDefaultModifier);

		// Complete the definition to use these generators and modifiers in some tests
		Stream.of(typeProperty, classProperty, customProperty)
				.forEach(p -> p.withGetter().withSetter());

		return def;
	}

	public static class Pojo {
		public int typeProperty;
		public int getTypeProperty() {return typeProperty;}
		public void setTypeProperty(int value) {this.typeProperty = value;}
		
		public int classProperty;
		public int getClassProperty() {return classProperty;}
		public void setClassProperty(int value) {this.classProperty = value;}
		
		public int customProperty;
		public int getCustomProperty() {return customProperty;}
		public void setCustomProperty(int value) {this.customProperty = value;}
	}
}
