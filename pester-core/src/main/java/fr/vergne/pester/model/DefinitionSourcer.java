package fr.vergne.pester.model;

import java.util.Arrays;
import java.util.function.Predicate;

public interface DefinitionSourcer {
	StackTraceElement[] getDefinitionStackTrace();

	default StackTraceElement[] createDefinitionStackTraceFromHere() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		Predicate<StackTraceElement> hasReachedDefinition = element -> "createPojoDefinition"
				.equals(element.getMethodName());
		int startIndex = 0;
		while (startIndex < stackTrace.length && !hasReachedDefinition.test(stackTrace[startIndex])) {
			startIndex++;
		}
		return Arrays.copyOfRange(stackTrace, startIndex, stackTrace.length);
	}
}
