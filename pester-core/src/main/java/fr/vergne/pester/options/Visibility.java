package fr.vergne.pester.options;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

public enum Visibility implements Option {
	PUBLIC(Modifier::isPublic),
	PROTECTED(Modifier::isProtected),
	PACKAGE(mod -> !Modifier.isPublic(mod) && !Modifier.isProtected(mod) && !Modifier.isPrivate(mod)),
	PRIVATE(Modifier::isPrivate);
	
	private final Predicate<Integer> modifierPredicate;
	
	private Visibility(Predicate<Integer> modifierPredicate) {
		this.modifierPredicate = modifierPredicate;
	}
	
	@Override
	public boolean testModifiers(int modifiers) {
		return modifierPredicate.test(modifiers);
	}
}
