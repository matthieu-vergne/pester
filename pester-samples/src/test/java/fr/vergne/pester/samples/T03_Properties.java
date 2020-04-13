package fr.vergne.pester.samples;

import static fr.vergne.pester.options.Mutability.*;
import static fr.vergne.pester.options.Scope.*;
import static fr.vergne.pester.options.Visibility.*;

import java.util.stream.Stream;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.samples.T03_Properties.Pojo;

class T03_Properties implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo.class);
		
		/*
		 * The first objective of Pester is to test that the expected properties are
		 * present and accessible as required. In other words, the main effort is to
		 * define these properties correctly.
		 */

		// A property can be supported by a field
		def.addProperty(byte.class, "fieldProperty")
				.withField();

		// A field can be more or less constrained
		def.addProperty(byte.class, "otherFieldProperty")
				.withField("customField", PUBLIC, NON_FINAL, NON_STATIC);

		// A property can be supported by a getter
		def.addProperty(byte.class, "getterProperty")
				.withGetter();

		// A getter can also be more or less constrained
		def.addProperty(byte.class, "otherGetterProperty")
				.withGetter("getCustomValue", PUBLIC, NON_FINAL, NON_STATIC);

		// A property can be supported by a setter
		def.addProperty(byte.class, "setterProperty")
				.withSetter();

		// A setter can also be more or less constrained
		def.addProperty(byte.class, "otherSetterProperty")
				.withSetter("setCustomValue", PUBLIC, NON_FINAL, NON_STATIC);
		
		// When a property has several supports, we also test their interactions
		def.addProperty(Integer.class, "multiSupportProperty")
				.withField(PRIVATE, NON_FINAL, NON_STATIC)
				.withGetter().withSetter();

		// If a default value is provided, additional tests are activated
		def.addProperty(String.class, "defaultProperty")
				.withField().withDefaultValue("my val");
		
		// Defaults values need a default constructor, we come back on constructors later
		def.addConstructor();

		// Common constraints on several properties can be easily factored
		Stream.of(
				def.addProperty(Object.class, "sharedDefProperty1"),
				def.addProperty(String.class, "sharedDefProperty2"),
				def.addProperty(int.class, "sharedDefProperty3"))
				.forEach(d -> d.withField(PUBLIC, NON_FINAL, NON_STATIC));

		// You can go as far as your skills and needs go
		Stream.of(1, 2, 3)
				.map(i -> "commonProperty" + i)
				.map(name -> def.addProperty(Object.class, name))
				.map(propDef -> propDef.withField(PUBLIC, NON_FINAL, NON_STATIC))
				// ...
				.forEach(propDef -> {});

		return def;
	}

	public static class Pojo {
		public byte fieldProperty;
		
		public byte customField;
		
		public byte getGetterProperty() {return 0;}

		public byte getCustomValue() {return 0;}

		public void setSetterProperty(byte value) {}

		public void setCustomValue(byte value) {}

		private Integer multiSupportProperty;
		public Integer getMultiSupportProperty() {return multiSupportProperty;}
		public void setMultiSupportProperty(Integer value) {multiSupportProperty = value;}

		public String defaultProperty = "my val";

		public Object sharedDefProperty1;
		public String sharedDefProperty2;
		public int sharedDefProperty3;

		public Object commonProperty1;
		public Object commonProperty2;
		public Object commonProperty3;
	}
}
