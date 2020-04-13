package fr.vergne.pester.samples;

import static fr.vergne.pester.options.Mutability.*;
import static fr.vergne.pester.options.Scope.*;
import static fr.vergne.pester.options.Visibility.*;

import java.util.stream.Stream;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;
import fr.vergne.pester.samples.T08_Constructors.Pojo;

class T08_Constructors implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo.class);
		
		// Add a default constructor, useful to test default values
		def.addConstructor();

		// You need to create properties for constructor arguments
		// Their generators and modifiers can be used to create initialization values
		def.addConstructor(def.addProperty(String.class), def.addProperty(int.class));

		// If the properties can be tested, we can test the impact of the constructor on them
		PropertyDefinition<Pojo, Boolean> property1 = def.addProperty(boolean.class, "property1");
		PropertyDefinition<Pojo, Integer> property2 = def.addProperty(int.class, "property2");
		PropertyDefinition<Pojo, String> property3 = def.addProperty(String.class, "property3");
		def.addConstructor(property1, property2, property3);
		Stream.of(property1, property2, property3)
				.forEach(property -> property.withField(PUBLIC, NON_FINAL, NON_STATIC));

		// You can also constrain the visibility of the constructor
		def.addConstructor(PRIVATE, def.addProperty(byte.class));
		
		// When you create a POJO definition, you may tell how to instantiate it
		new PojoDefinition<>(() -> new Pojo());
		
		// If you provide constructors, you can provide only the class
		// It will use one of them to instantiate the POJO
		new PojoDefinition<>(Pojo.class);

		return def;
	}

	@SuppressWarnings("unused")
	public static class Pojo {
		public Pojo() {
			// Instantiate with default values
		}

		public Pojo(boolean property1, int property2, String property3) {
			this.property1 = property1;
			this.property2 = property2;
			this.property3 = property3;
		}

		public Pojo(String str, int integer) {
			this.property1 = str.isEmpty();
			this.property2 = integer;
			this.property3 = str;
		}

		private Pojo(byte value) {
			// Constructor with specific visibility
		}

		public boolean property1;
		public int property2;
		public String property3;
	}
}
