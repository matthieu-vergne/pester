package fr.vergne.pester.samples;

import static fr.vergne.pester.options.Mutability.*;
import static fr.vergne.pester.options.Scope.*;
import static fr.vergne.pester.options.Visibility.*;

import java.util.function.Supplier;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.definition.PropertyDefinition;
import fr.vergne.pester.samples.T10_Errors.Pojo;
import fr.vergne.pester.value.Type;

// TODO Fix definition sources: some have the wrong source
// TODO Fix missing tests: some remain disabled despite having material
// TODO Fix class definition sources: cf. warning logs
class T10_Errors implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo::new);
		Type<?> unconstrainedType = def.create().type().as("unconstrainedType")
				.withGenerator(() -> new Object())
				.withModifier(obj -> new Object());
		
		// Not extended class
		def.setParentClass(String.class);
		
		// Not implemented interface
		def.addInterfaces(Supplier.class);
		
		// Absent constructor
		def.addConstructor(def.addProperty(byte.class));
		
		// Non private constructor
		def.addConstructor(PRIVATE, def.addProperty(int.class));
		
		// Constructor which throws exception
		def.addConstructor(def.addProperty(String.class));
		
		// Absent field/getter/setter
		def.addProperty(unconstrainedType, "absentProperty")
				.withField()
				.withGetter()
				.withSetter();
		
		// Wrong type on field/getter/setter
		def.addProperty(Object.class, "byteProperty")
				.withField()
				.withGetter()
				.withSetter();
		
		// Not linked field/getter/setter/constructor
		PropertyDefinition<Pojo, Object> ignoredProperty = def.addProperty(Object.class, "ignoredProperty")
				.withField()
				.withGetter()
				.withSetter()
				.withDefaultValue(new Object());
		def.addConstructor(ignoredProperty);
		def.addConstructor();
		
		// Wrong options on field/getter/setter
		def.addProperty(unconstrainedType, "property")
				.withField(PRIVATE, FINAL, STATIC)
				.withGetter(PRIVATE, FINAL, STATIC)
				.withSetter(PRIVATE, FINAL, STATIC);
		
		// Wrong options on field/getter/setter
		def.addProperty(unconstrainedType, "exceptionProperty")
				.withGetter()
				.withSetter();
		
		return def;
	}
	
	public static class Pojo {
		public Pojo() {
		}
		public Pojo(Object ignoredProperty) {
		}
		public Pojo(int value) {
		}
		public Pojo(String value) {
			throw new RuntimeException("some exception");
		}
		
		byte byteProperty;
		
		Object property;
		public Object getProperty() {return property;}
		public void setProperty(Object value) {this.property = value;}
		
		Object ignoredProperty;
		public Object getIgnoredProperty() {return null;}
		public void setIgnoredProperty(Object value) {}
		
		public byte getExceptionProperty() {throw new RuntimeException("some exception");}
		public void setExceptionProperty(Object value) {throw new RuntimeException("some exception");}
	}
}
