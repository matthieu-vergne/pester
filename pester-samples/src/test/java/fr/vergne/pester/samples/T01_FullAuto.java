package fr.vergne.pester.samples;

import static fr.vergne.pester.junit.annotation.ConstructorPropertiesHelper.*;

import java.beans.ConstructorProperties;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.DefinitionFactory;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.samples.T01_FullAuto.Pojo;

class T01_FullAuto implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		return new DefinitionFactory().guessFromClass(Pojo.class);
	}

	public static class Pojo {
		public Pojo() {
			// Default constructor
			this.accessorProperty = 123;
		}
		
		@ConstructorProperties({"accessorProperty"})
		// Constructor with known properties
		public Pojo(Integer accessorProperty) {
			this.accessorProperty = accessorProperty;
			/**
			 * To know whether the parameters corresponds to properties with fields or
			 * accessors, we rely on the common name between both. Unfortunately, the
			 * parameter names are not stored unless a specific compilation argument is
			 * provided. If it is the case, we retrieve them, otherwise we can't, and thus
			 * we cannot know whether the parameters correspond to known properties.
			 * 
			 * The {@link ConstructorProperties} annotation fixes that by attaching this
			 * information to the constructor. This is a standard JavaBean annotation, not
			 * something specific to Pester. We exploit it in priority to find whether this
			 * constructor is linked to existing properties.
			 * 
			 * Without this information, we cannot activate tests that map the constructor
			 * and the fields or accessors of these properties. Warnings on that matter are
			 * generated when running the test.
			 */
		}
		
		@ConstructorProperties({NON_PROPERTY})
		// Constructor with dedicated properties
		public Pojo(String unknownProperty) {
			this.accessorProperty = unknownProperty.length();
			/**
			 * This constructor's parameter does not correspond to a known property, so we
			 * use the {@link ConstructorProperties} to explicit that. Otherwise, the
			 * information would be missing and a warning would appear for this constructor.
			 */
		}
		
		// Constructor with dedicated properties
		public Pojo(Object[] yetAnotherProperty) {
			this.accessorProperty = yetAnotherProperty.length;
			/**
			 * This constructor is not annotated, so as long as you don't change your
			 * compiler options, you should see a warning about this constructor when
			 * running the test.
			 */
		}
		
		public byte directAccessProperty;

		private Integer accessorProperty;
		public Integer getAccessorProperty() {return accessorProperty;}
		public void setAccessorProperty(Integer value) {accessorProperty = value;}
	}
}
