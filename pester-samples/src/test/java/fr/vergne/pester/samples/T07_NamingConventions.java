package fr.vergne.pester.samples;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.samples.T07_NamingConventions.Pojo;

class T07_NamingConventions implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo::new);

		/*
		 * Naming is free, but there is common conventions that we rely on to find the
		 * elements related to the various properties.
		 */
		
		// A field usually has the name of the property
		def.addProperty(byte.class, "myProperty")
				.withField(); // Will find myProperty
		
		// If you don't do that, just explicit the name of the field
		def.addProperty(byte.class, "myOtherProperty")
				.withField("myAwesomeField"); // Will find myProperty
		
		// Getters and setters also have the usual getXxx and setXxx convention
		def.addProperty(byte.class, "classical")
				.withGetter()  // Will find getClassical()
				.withSetter(); // Will find setClassical(byte)
		
		// We also find them if they are named like the property
		def.addProperty(byte.class, "propertyName")
				.withGetter()  // Will find propertyName()
				.withSetter(); // Will find propertyName(byte)

		// The boolean convention consists in a isXxx getter
		def.addProperty(boolean.class, "booleanProperty")
				.withGetter(); // Will find isBooleanProperty()

		// We also cover the withXxx convention for setters
		def.addProperty(byte.class, "anotherProperty")
				.withSetter(); // Will find withAnotherProperty(byte)

		// If you have your own conventions, just explicit them
		def.addProperty(byte.class, "customProperty")
				.withGetter("myAwesomeGetter")      // Will find myAwesomeGetter()
				.withSetter("myMagnificentSetter"); // Will find myMagnificentSetter(byte)

		return def;
	}

	public static class Pojo {
		byte myProperty;
		byte myAwesomeField;
		
		byte classical;
		void setClassical(byte value) {this.classical = value;}
		byte getClassical() {return classical;}
		
		byte propertyName;
		void propertyName(byte value) {this.propertyName = value;}
		byte propertyName() {return propertyName;}
		
		boolean booleanProperty;
		boolean isBooleanProperty() {return booleanProperty;}
		
		byte anotherProperty;
		void withAnotherProperty(byte value) {this.anotherProperty = value;}
		
		byte customProperty;
		byte myAwesomeGetter() {return customProperty;}
		void myMagnificentSetter(byte value) {this.customProperty = value;}
	}
}
