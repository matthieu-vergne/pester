package fr.vergne.pester.samples;

import java.util.Map;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.samples.T06_BasicAndAdvancedTypes.Pojo;
import fr.vergne.pester.value.Type;

class T06_BasicAndAdvancedTypes implements PesterTest<Pojo> {

	@SuppressWarnings("unused")
	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo.class, Pojo::new);
		
		// Each primitive class is supported, including default generators and modifiers
		def.addProperty(byte.class, "byteProperty");
		def.addProperty(short.class, "shortProperty");
		def.addProperty(int.class, "intProperty");
		def.addProperty(long.class, "longProperty");
		def.addProperty(float.class, "floatProperty");
		def.addProperty(double.class, "doubleProperty");
		def.addProperty(boolean.class, "boolProperty");
		def.addProperty(char.class, "charProperty");
		
		// The boxed types are supported as well
		def.addProperty(Byte.class, "byteObjectProperty");
		def.addProperty(Short.class, "shortObjectProperty");
		def.addProperty(Integer.class, "intObjectProperty");
		def.addProperty(Long.class, "longObjectProperty");
		def.addProperty(Float.class, "floatObjectProperty");
		def.addProperty(Double.class, "doubleObjectProperty");
		def.addProperty(Boolean.class, "boolObjectProperty");
		def.addProperty(Character.class, "charObjectProperty");
		
		// For basic testing, any kind of object is welcome
		def.addProperty(Object.class, "objectProperty");
		def.addProperty(String.class, "stringProperty");
		def.addProperty(Custom.class, "customProperty");
		
		// For tests which need concrete values, you need generators and modifiers
		def.create().type().from(Custom.class)
				.withGenerator(() -> new Custom())
				.withModifier(instance -> new Custom());
		
		// Array types are created automatically
		// A (custom) type with generator and modifier allows to generate arrays of it
		def.addProperty(byte[].class, "byteArray");
		def.addProperty(Byte[].class, "byteObjectArray");
		def.addProperty(Custom[].class, "customArray");
		def.addProperty(Object[][][].class, "multiArray");
		
		// For generators and modifiers, you can build on default ones
		def.create().generator().ofBytes(); def.create().modifier().ofBytes();
		def.create().generator().ofBytes(); def.create().modifier().ofBytes();
		def.create().generator().ofShorts(); def.create().modifier().ofShorts();
		def.create().generator().ofShorts(); def.create().modifier().ofShorts();
		def.create().generator().ofIntegers(); def.create().modifier().ofIntegers();
		def.create().generator().ofIntegers(); def.create().modifier().ofIntegers();
		def.create().generator().ofLongs(); def.create().modifier().ofLongs();
		def.create().generator().ofLongs(); def.create().modifier().ofLongs();
		def.create().generator().ofFloats(); def.create().modifier().ofFloats();
		def.create().generator().ofFloats(); def.create().modifier().ofFloats();
		def.create().generator().ofDoubles(); def.create().modifier().ofDoubles();
		def.create().generator().ofDoubles(); def.create().modifier().ofDoubles();
		def.create().generator().ofBooleans(); def.create().modifier().ofBooleans();
		def.create().generator().ofBooleans(); def.create().modifier().ofBooleans();
		def.create().generator().ofCharacters(); def.create().modifier().ofCharacters();
		def.create().generator().ofCharacters(); def.create().modifier().ofCharacters();
		def.create().generator().ofStrings(); def.create().modifier().ofStrings();
		def.create().generator().ofObjects(); def.create().modifier().ofObjects();
		
		// There is also supports for some generic types as well
		def.create().generator().ofLists(int.class); def.create().modifier().ofLists(int.class);
		def.create().generator().ofSets(int.class); def.create().modifier().ofSets(int.class);
		def.create().generator().ofMaps(String.class, Object.class); def.create().modifier().ofMaps(String.class, Object.class);
		
		// Don't hesitate to combine them to create complex types
		Type<String> keyType = def.create().type().from(String.class);
		Type<Custom> valueType = def.create().type().from(Custom.class);
		Type<Map<String, Custom>> complexType = def.create().type().
				<Map<String, Custom>> as("complex type", Map.class)
				.withGenerator(def.create().generator().ofMaps(keyType, valueType))
				.withModifier(def.create().modifier().ofMaps(keyType, valueType));
		
		// Require a field for each property to activate some tests
		def.getProperties().stream()
			.forEach(property -> property.withField());

		return def;
	}

	public static class Pojo {
		byte byteProperty;
		short shortProperty;
		int intProperty;
		long longProperty;
		float floatProperty;
		double doubleProperty;
		boolean boolProperty;
		char charProperty;

		Byte byteObjectProperty;
		Short shortObjectProperty;
		Integer intObjectProperty;
		Long longObjectProperty;
		Float floatObjectProperty;
		Double doubleObjectProperty;
		Boolean boolObjectProperty;
		Character charObjectProperty;

		Object objectProperty;
		String stringProperty;
		Custom customProperty;
		
		byte[] byteArray;
		Byte[] byteObjectArray;
		Custom[] customArray;
		Object[][][] multiArray;
	}
	
	public static class Custom {}
	public static class Generic<T> {}
}
