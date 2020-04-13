package fr.vergne.pester.samples;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.samples.T09_ImplementsAndExtends.Pojo;

class T09_ImplementsAndExtends implements PesterTest<Pojo> {

	@Override
	public PojoDefinition<Pojo> createPojoDefinition() {
		PojoDefinition<Pojo> def = new PojoDefinition<>(Pojo.class);
		
		// You can constrain your POJO to extend a parent class
		def.setParentClass(ParentClass.class);
		
		// You can constrain your POJO to implement interfaces
		def.addInterfaces(Interface1.class, Interface2.class);
		
		// Derived interfaces (from parents and interfaces) are covered as well
		def.addInterfaces(DerivedInterface.class);
		
		// Duplicates are ignored
		def.addInterfaces(Interface1.class, Interface1.class);
		
		return def;
	}
	
	public static interface DerivedInterface {}
	public static class ParentClass implements DerivedInterface {}
	
	public static interface Interface1 {}
	public static interface Interface2 {}
	public static class Pojo extends ParentClass implements Interface1, Interface2 {}
}
