package fr.vergne.pester.junit;

import static fr.vergne.pester.junit.TestParameter.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.platform.commons.support.AnnotationSupport;

import fr.vergne.pester.junit.annotation.DefinitionSource;
import fr.vergne.pester.util.cache.Cache;
import fr.vergne.pester.util.indexer.IndexedValue;
import fr.vergne.pester.util.indexer.Indexer;
import fr.vergne.pester.util.indexer.impl.IteratorIndexer;
import fr.vergne.pester.util.optional.BiOptional;

public class PesterTestComparator implements Comparator<Method> {

	private final Comparator<Method> comparator = initComparator(Method.class)
			.thenComparing(byTestParameters())
			.thenComparing(byTestNames());
	
	@Override
	public int compare(Method m1, Method m2) {
		return comparator.compare(m1, m2);
	}
	
	private Comparator<Method> byTestParameters() {
		Function<Method, TestParameter[]> toTestParameters = method -> AnnotationSupport
				.findAnnotation(method, DefinitionSource.class)
				.map(DefinitionSource::value)
				.orElse(null);
		
		Comparator<TestParameter[]> testParametersComparator = initComparator(TestParameter[].class)
				.thenComparing(byTestedParametersPriority())
				.thenComparing(byFewerParametersFirst())
				.thenComparing(byParametersPriority());
		
		return (method1, method2) -> {
			return BiOptional.ofNullables(method1, method2)
					.mapEach(toTestParameters)
					.mapBoth(
							// Compare parameters-based methods
							(tp1, tp2) -> testParametersComparator.compare(tp1, tp2),
							// Prioritize components-based methods
							tp1 -> -1,
							tp2 -> 1)
					// In other cases, we just don't know
					.orElse(0);
		};
	}
	
	private Comparator<TestParameter[]> byTestedParametersPriority() {
		// Parameters are stored in sets to ease retrieval.
		Function<TestParameter[], Set<TestParameter>> parametersAdapter = parameters -> new HashSet<>(Arrays.asList(parameters));
		
		// Use a cache to not rebuild the set each time.
		Function<TestParameter[], Set<TestParameter>> cache = Cache.onFunction(parametersAdapter);
		
		// Identify the tested parameters.
		Predicate<TestParameter[]> pojoClass = parameters -> cache.apply(parameters).contains(POJO_CLASS);
		Predicate<TestParameter[]> constructor = parameters -> cache.apply(parameters).contains(CONSTRUCTOR) || cache.apply(parameters).contains(DEFAULT_CONSTRUCTOR);
		Predicate<TestParameter[]> field = parameters -> cache.apply(parameters).contains(FIELD) || cache.apply(parameters).contains(MUTABLE_FIELD);
		Predicate<TestParameter[]> getter = parameters -> cache.apply(parameters).contains(GETTER);
		Predicate<TestParameter[]> setter = parameters -> cache.apply(parameters).contains(SETTER);
		Predicate<TestParameter[]> noField = field.negate();
		Predicate<TestParameter[]> noGetter = getter.negate();
		Predicate<TestParameter[]> noSetter = setter.negate();
		
		// Identify the different categories (patterns of parameters).
		// They are ordered by priority to establish the comparison.
		List<Predicate<TestParameter[]>> categories = Arrays.asList(
				pojoClass,
				constructor.and(noField).and(noGetter),
				constructor.and(field).and(noGetter),
				constructor.and(noField).and(getter),
				constructor.and(field).and(getter),
				field.and(noGetter).and(noSetter),
				noField.and(getter).and(noSetter),
				field.and(getter).and(noSetter),
				noField.and(noGetter).and(setter),
				field.and(noGetter).and(setter),
				noField.and(getter).and(setter),
				field.and(getter).and(setter));
		
		// Compute a score for the given parameters.
		// This score corresponds to the index of the situation observed.
		Function<TestParameter[], Integer> scoring = parameters -> {
			Indexer<Boolean> indexer = new IteratorIndexer<>();
			return categories.stream()
					.map(category -> category.test(parameters))
					.map(indexer::decorateWithIndex)
					.filter(IndexedValue::getValue)
					.map(IndexedValue::getIndex)
					.findFirst()
					.orElseGet(() -> {throw new RuntimeException("Situation not considered: " + Arrays.deepToString(parameters));});
		};
		
		// Compare parameters based on their situations
		return (parameters1, parameters2) -> scoring.apply(parameters1).compareTo(scoring.apply(parameters2));
	}
	
	private Comparator<TestParameter[]> byFewerParametersFirst() {
		return (parameters1, parameters2) -> Integer.compare(parameters1.length, parameters2.length);
	}
	
	private Comparator<TestParameter[]> byParametersPriority() {
		// A single parameter is assigned a score based on its priority.
		// The priority is decided by the position of the parameter in its enum.
		// A better score corresponds to a higher place, so a lower score (smaller index in the enum).
		Function<TestParameter, Integer> parameterScorer = Arrays.asList(TestParameter.values())::indexOf;
		
		// The score of a list of parameters is based on the score of each of them.
		// They are sorted by increasing score to ease the comparison.
		Function<TestParameter[], List<Integer>> parametersScorer = parameters -> Arrays
				.asList(parameters).stream()
				.map(parameterScorer)
				.sorted()
				.collect(Collectors.toList());
		
		// The parameters are compared based on which shows the lowest score.
		return (parameters1, parameters2) -> {
			List<Integer> scores1 = parametersScorer.apply(parameters1);
			List<Integer> scores2 = parametersScorer.apply(parameters2);
			for (int i = 0; i < parameters1.length; i++) {
				int comparison = scores1.get(i).compareTo(scores2.get(i));
				if (comparison != 0) {
					return comparison;
				}
			}
			return 0;
		};
	}
	
	private Comparator<Method> byTestNames() {
		return Comparator.comparing(Method::getName);
	}
	
	private <T> Comparator<T> initComparator(Class<T> clazz) {
		return (Comparator<T>) (a,b) -> 0;
	}
}
