package fr.vergne.pester.definition;

@SuppressWarnings("serial")
public class AlreadyDefinedPropertyException extends IllegalArgumentException {
	public AlreadyDefinedPropertyException(String propertyName) {
		super(propertyName + " is already defined");
	}
}
