package fr.vergne.pester.options;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

public enum Mutability implements Option {
	FINAL(Modifier::isFinal), NON_FINAL(mod -> !Modifier.isFinal(mod));
	
	private final Predicate<Integer> modifierPredicate;
	
	private Mutability(Predicate<Integer> modifierPredicate) {
		this.modifierPredicate = modifierPredicate;
	}
	
	@Override
	public boolean testModifiers(int modifiers) {
		return modifierPredicate.test(modifiers);
	}
}
