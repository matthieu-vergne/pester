package fr.vergne.pester.samples;

import java.util.List;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;
import fr.vergne.pester.samples.T04_ClassesAndTypes.Pojo;
import fr.vergne.pester.value.Type;

class T04_ClassesAndTypes implements PesterTest<Pojo> {

	@SuppressWarnings("unused")
	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo::new);

		// Properties can be typed with a class
		PropertyDefinition<Pojo, Byte> classProperty = def.addProperty(byte.class, "classProperty");

		// Properties can also be typed with a type object
		Type<Byte> type = def.create().type().from(byte.class);
		PropertyDefinition<Pojo, Byte> typeProperty = def.addProperty(type, "typeProperty");
		
		// A class-based type is always the same for the same class
		Type<String> classType1 = def.create().type().from(String.class);
		Type<String> classType2 = def.create().type().from(String.class);
		boolean classEquals = classType1.equals(classType2);// true
		System.out.println(String.format("%s == %s ? %s", classType1, classType2, classEquals));
		
		// You can also create named types, which are separate instances
		// Names are not constrained, they can be the same for practical purposes
		Type<String> namedType1 = def.create().type().as("String 1");
		Type<String> namedType2 = def.create().type().as("String 2");
		boolean customEquals = namedType1.equals(namedType2);// false
		System.out.println(String.format("%s == %s ? %s", namedType1, namedType2, customEquals));
		
		// Generic classes cannot be differentiated from their class, so you must create named types
		Type<List<Integer>> genericType1 = def.create().type().as("List<Integer>");
		Type<List<Long>> genericType2 = def.create().type().as("List<Long>");
		boolean genericEquals = genericType1.equals(genericType2);// false
		System.out.println(String.format("%s == %s ? %s", genericType1, genericType2, genericEquals));
		
		/*
		 * The first purpose of named types is to differentiate types having the same
		 * class. Since it covers generics, named types usually have no related class,
		 * because we cannot require a class which defines the generics. It is however
		 * possible to add a specific class:
		 */
		Type<List<Integer>> constrainedType = def.create().type().as("List<Integer>", List.class);
		
		/*
		 * Adding this class allows to enable the class-related tests, which are
		 * otherwise disabled due to the lack of information about the exact class to
		 * expect.
		 * 
		 * However, since the class cannot cover the generics, we cannot correlate it to
		 * the kind of type object returned. In the above example, we create a
		 * Type<List<Integer>>, but it would be impossible if we correlate it to the
		 * List.class in argument, which provides no generics. To allow generics in the
		 * returned type, the class is decorrelated, which means we could as well
		 * provide a Set.class or even String.class:
		 */
		Type<List<Integer>> incoherentType = def.create().type().as("List<Integer>", String.class);
		
		/*
		 * In practice, it only means that we cannot control the alignment of these
		 * types at compilation time. But the error will appear at runtime: when testing
		 * the class, it will expect a String, but since the Type object is about
		 * List<Integer>, you can only assign List<Integer> values. In this condition,
		 * the class tests will necessarily fail. So pay attention to the classes you
		 * provide if you do so.
		 */
		
		// Let's add some properties to illustrate some valid named types
		PropertyDefinition<Pojo,String> namedProperty = def.addProperty(namedType1, "namedProperty");
		PropertyDefinition<Pojo,List<Integer>> genericProperty = def.addProperty(genericType1, "genericProperty");
		PropertyDefinition<Pojo,List<Integer>> constrainedProperty = def.addProperty(constrainedType, "constrainedProperty");
		
		// Complete the definition to activate some tests
		def.getProperties().forEach(p -> p.withField());

		return def;
	}

	public static class Pojo {
		byte classProperty;
		byte typeProperty;
		String namedProperty;
		List<Integer> genericProperty;
		List<Integer> constrainedProperty;
	}
}
