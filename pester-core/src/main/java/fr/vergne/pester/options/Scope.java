package fr.vergne.pester.options;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

public enum Scope implements Option {
	STATIC(Modifier::isStatic), NON_STATIC(mod -> !Modifier.isStatic(mod));
	
	private final Predicate<Integer> modifierPredicate;
	
	private Scope(Predicate<Integer> modifierPredicate) {
		this.modifierPredicate = modifierPredicate;
	}
	
	@Override
	public boolean testModifiers(int modifiers) {
		return modifierPredicate.test(modifiers);
	}
}
