package fr.vergne.pester.util.argscheck;

public interface ArgsCheck {

	public static <T> T requireNonNull(T arg, String message) {
		if (arg == null) {
			throw new IllegalArgumentException(message);
		} else {
			return arg;
		}
	}
	
	public static String requireNonNullNorEmpty(String arg, String message) {
		if (arg == null || arg.isEmpty()) {
			throw new IllegalArgumentException(message);
		} else {
			return arg;
		}
	}
}
