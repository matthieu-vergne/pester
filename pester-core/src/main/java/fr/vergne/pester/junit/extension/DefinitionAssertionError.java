package fr.vergne.pester.junit.extension;

@SuppressWarnings("serial")
public class DefinitionAssertionError extends AssertionError {
	public DefinitionAssertionError(String message, Throwable cause) {
		super(message, cause);
	}
}
