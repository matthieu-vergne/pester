package fr.vergne.pester.junit.extension;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.platform.commons.support.AnnotationSupport;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.junit.TestCasesGenerator;
import fr.vergne.pester.junit.TestParameter;
import fr.vergne.pester.junit.annotation.DefinitionSource;
import fr.vergne.pester.util.cache.Cache;
import fr.vergne.pester.util.cache.Key;
import fr.vergne.pester.util.cache.ParameteredKey;

public class DefinitionCasesProvider implements ArgumentsProvider {
	
	public static final Key<List<Object[]>> PARAMETERIZED_TEST_CASES = ParameteredKey.create(new Object());
	
	@Override
	public Stream<Arguments> provideArguments(ExtensionContext context) throws Exception {
		Cache cache = new ExtensionCache(context).getTestCache(context.getUniqueId());
		List<Object[]> testCases = cache.get(PARAMETERIZED_TEST_CASES, LinkedList::new);
		return TestCasesGenerator
				.streamTestCases(getDefinition(context), getTestParameters(context))
				.peek(args -> testCases.add(args.get())); // Cache them for reuse elsewhere
	}
	
	private PojoDefinition<?> getDefinition(ExtensionContext context) {
		PesterTest<?> testInstance = (PesterTest<?>) context.getRequiredTestInstance();
		return new ExtensionCache(context).getPojoDefinition(testInstance);
	}
	
	private TestParameter[] getTestParameters(ExtensionContext context) {
		return AnnotationSupport.findAnnotation(
				context.getRequiredTestMethod(),
				DefinitionSource.class)
				.get().value();
	}
}
