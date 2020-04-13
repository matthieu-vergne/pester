package fr.vergne.pester.testutil;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

public class ArgumentsSupport {
	public static Function<Arguments, Stream<Arguments>> appendArg(Object... argValues) {
		return appendArg(() -> Stream.of(argValues));
	}

	public static Function<Arguments, Stream<Arguments>> appendArg(Supplier<Stream<?>> argValues) {
		return appendArgReadAll(args -> argValues.get());
	}

	public static Function<Arguments, Stream<Arguments>> appendArgReadAll(Function<Object[], Stream<?>> argValues) {
		return args -> argValues.apply(args.get()).map(argValue -> {
			Object[] oldArgs = args.get();
			Object[] newArgs = Arrays.copyOf(oldArgs, oldArgs.length + 1);
			newArgs[oldArgs.length] = argValue;
			return Arguments.of(newArgs);
		});
	}

	public static Function<Arguments, Stream<Arguments>> replaceArg(int argIndex, Function<Object, Stream<?>> replacer) {
		return replaceArgReadAll(argIndex, args -> replacer.apply(args[argIndex]));
	}

	public static Function<Arguments, Stream<Arguments>> replaceArgReadAll(int argIndex,
			Function<Object[], Stream<?>> replacer) {
		return args -> {
			Object[] oldArgs = args.get();
			Stream<?> newValues = replacer.apply(oldArgs);
			return newValues.map(newValue -> {
				Object[] newArgs = Arrays.copyOf(oldArgs, oldArgs.length);
				newArgs[argIndex] = newValue;
				return Arguments.of(newArgs);
			});
		};
	}
}
