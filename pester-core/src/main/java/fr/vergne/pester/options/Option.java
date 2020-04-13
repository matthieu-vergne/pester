package fr.vergne.pester.options;

import java.util.stream.Stream;

public interface Option {
	public boolean testModifiers(int modifiers);

	public static <E extends Option> E getFromModifiers(int modifiers, Class<E> optionClass) {
		return Stream.of(optionClass.getEnumConstants())
				.filter(v -> v.testModifiers(modifiers))
				.findFirst()
				.orElseThrow(() -> new RuntimeException(String.format("No %s corresponds to modifiers: %d", optionClass.getSimpleName(), modifiers)));
	}
}
