package fr.vergne.pester.samples;

import java.io.Serializable;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.DefinitionFactory;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.samples.T02_JavaBean.Pojo;

class T02_JavaBean implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		return new DefinitionFactory().fromBeanClass(Pojo.class);
	}

	@SuppressWarnings("serial")
	public static class Pojo implements Serializable {
		public Pojo() {
			// Default constructor, can be implicit
		}
		
		private Integer property1;
		public Integer getProperty1() {return property1;}
		public void setProperty1(Integer value) {property1 = value;}
		
		private Integer property2;
		public Integer getProperty2() {return property2;}
		public void setProperty2(Integer value) {property2 = value;}
	}
}
