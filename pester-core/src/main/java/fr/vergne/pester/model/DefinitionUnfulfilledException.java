package fr.vergne.pester.model;

@SuppressWarnings("serial")
public class DefinitionUnfulfilledException extends RuntimeException {
	public DefinitionUnfulfilledException(String message) {
		super(message);
	}
	
	public DefinitionUnfulfilledException(String message, Throwable cause) {
		super(message, cause);
	}
}
