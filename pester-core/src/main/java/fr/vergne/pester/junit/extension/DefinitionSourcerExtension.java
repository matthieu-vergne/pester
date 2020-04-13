package fr.vergne.pester.junit.extension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.support.AnnotationSupport;

import fr.vergne.pester.factory.IncompleteDefinitionException;
import fr.vergne.pester.junit.TestParameter;
import fr.vergne.pester.junit.annotation.DefinitionSource;
import fr.vergne.pester.junit.annotation.TestTarget;
import fr.vergne.pester.model.DefinitionSourcer;
import fr.vergne.pester.model.DefinitionUnfulfilledException;
import fr.vergne.pester.util.cache.Cache;

public class DefinitionSourcerExtension implements TestExecutionExceptionHandler {

	private static final Logger LOGGER = Logger.getLogger(DefinitionSourcerExtension.class.getName());

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable cause) throws Throwable {
		Method testMethod = context.getTestMethod().get();
		Optional<DefinitionSource> sourceAnnot = AnnotationSupport.findAnnotation(testMethod, DefinitionSource.class);
		Optional<TestTarget> targetAnnot = AnnotationSupport.findAnnotation(testMethod, TestTarget.class);
		if (!sourceAnnot.isPresent()) {
			// Seems to be a regular test, don't do anything specific
		} else if (!targetAnnot.isPresent()) {
			LOGGER.warning(String.format(
					"The test %s is not annotated with %s, so we cannot source test failures to their specific definitions.",
					testMethod, TestTarget.class.getName()));
		} else {
			TestParameter parameter = targetAnnot.get().value();
			int parameterIndex = Arrays.asList(sourceAnnot.get().value()).indexOf(parameter);
			Object argument = getTestCaseArguments(context)[parameterIndex];

			if (!(argument instanceof DefinitionSourcer)) {
				LOGGER.warning(String.format(
						"The class %s does not implement %s, so we cannot source its test failures to their specific definitions.",
						argument.getClass().getName(), DefinitionSourcer.class.getName()));
			} else {
				DefinitionSourcer sourcer = (DefinitionSourcer) argument;
				StackTraceElement[] definitionStackTrace = sourcer.getDefinitionStackTrace();
				Throwable rootCause;
				if (cause instanceof AssertionError || cause instanceof DefinitionUnfulfilledException) {
					rootCause = new DefinitionAssertionError("Definition of " + sourcer + " not fulfilled ==> " + cause.getLocalizedMessage(), cause);
				} else if (cause instanceof IncompleteDefinitionException) {
					rootCause = new RuntimeException("Definition of " + sourcer + " incomplete ==> " + cause.getLocalizedMessage(), cause);
				} else {
					rootCause = new RuntimeException("An unexpected exception occurred in Pester, please contact the Pester team", cause);
				}
				rootCause.setStackTrace(definitionStackTrace);
				throw rootCause;
			}
		}
		throw cause; // By default, just throw again the exception
	}
	
	private Object[] getTestCaseArguments(ExtensionContext context) {
		List<Object[]> testCases = getParameterizedTestCases(context);
		int testCaseIndex = getParameterizedTestCaseIndex(context);
		return testCases.get(testCaseIndex);
	}

	private List<Object[]> getParameterizedTestCases(ExtensionContext context) {
		ExtensionContext parentContext = context.getParent().get();
		Cache cache = new ExtensionCache(context).getTestCache(parentContext.getUniqueId());
		return cache.get(DefinitionCasesProvider.PARAMETERIZED_TEST_CASES);
	}

	private int getParameterizedTestCaseIndex(ExtensionContext context) {
		String contextId = context.getUniqueId();
		String invocationNumber = contextId.replaceFirst(".*test-template-invocation:#(\\d+).*", "\\1");
		int oneBasedInvocationIndex = Integer.parseInt(invocationNumber);
		return oneBasedInvocationIndex - 1;
	}
}
