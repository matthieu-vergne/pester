package fr.vergne.pester.factory;

@SuppressWarnings("serial")
public class IncompleteDefinitionException extends RuntimeException {
	public IncompleteDefinitionException(String message) {
		super(message);
	}
}
