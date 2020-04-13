package fr.vergne.pester.junit.extension;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.*;

import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.AnnotationSupport;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.junit.TestCasesGenerator;
import fr.vergne.pester.junit.TestParameter;
import fr.vergne.pester.junit.annotation.DefinitionSource;

/**
 * {@link EmptyParameterizedDefinitionTestsDisabler} disables any
 * {@link ParameterizedTest} based on a {@link DefinitionSource} annotation
 * which generates no test case. By default, a {@link ParameterizedTest} with no
 * test case throws a {@link PreconditionViolationException}. We disable the
 * test to avoid this.
 */
public class EmptyParameterizedDefinitionTestsDisabler implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		try {
			return context.getElement()
					.filter(Method.class::isInstance)
					.map(Method.class::cast)
					.filter(method -> AnnotationSupport.isAnnotated(method, ParameterizedTest.class))
					.filter(method -> AnnotationSupport.isAnnotated(method, DefinitionSource.class))
					.map(method -> hasCases(context, method))
					.map(hasCases -> hasCases ? enabled("has at least one case") : disabled("has no case"))
					.orElse(enabled("enabled by default"));
		} catch (CannotCheckCasesPresenceException cause) {
			// Let the test go to not wrap the cause into an extension exception
			return enabled("enabled to generate exception upon test execution");
		}
	}

	private boolean hasCases(ExtensionContext context, Method testMethod) {
		PojoDefinition<?> definition = getPojoDefinition(context);
		TestParameter[] parameters = getTestParameters(testMethod);
		try {
			return TestCasesGenerator.streamTestCases(definition, parameters)
					.findAny().isPresent();
		} catch (Exception cause) {
			throw new CannotCheckCasesPresenceException(cause);
		}
	}
	
	@SuppressWarnings("serial")
	private static class CannotCheckCasesPresenceException extends RuntimeException {
		public CannotCheckCasesPresenceException(Throwable cause) {
			super(cause);
		}
	}

	private PojoDefinition<?> getPojoDefinition(ExtensionContext context) {
		PesterTest<?> testInstance = (PesterTest<?>) context.getRequiredTestInstance();
		return new ExtensionCache(context).getPojoDefinition(testInstance);
	}
	
	private TestParameter[] getTestParameters(Method testMethod) {
		return AnnotationSupport.findAnnotation(testMethod, DefinitionSource.class).get().value();
	}
}
