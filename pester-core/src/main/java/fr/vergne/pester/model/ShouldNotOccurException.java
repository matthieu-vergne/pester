package fr.vergne.pester.model;

@SuppressWarnings("serial")
public class ShouldNotOccurException extends RuntimeException {
	public ShouldNotOccurException(Throwable cause) {
		super("Something unexpected occurred within Pester", cause);
	}
}
