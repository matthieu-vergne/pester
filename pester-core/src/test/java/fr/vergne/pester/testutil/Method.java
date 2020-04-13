package fr.vergne.pester.testutil;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Method {
	private java.lang.reflect.Method method;
	private Object[] args;

	public Method(Class<?> clazz, String methodName, Object... args) throws NoSuchMethodException, SecurityException {
		this.method = Stream.of(clazz.getDeclaredMethods())
				.filter(method -> method.getName().equals(methodName) && align(method.getParameterTypes(), args))
				.findAny().get();
		this.args = args;
	}

	private boolean align(Class<?>[] classes, Object[] values) {
		if (classes.length != values.length) {
			return false;
		}
		for (int i = 0; i < values.length; i++) {
			Class<?> c = classes[i];
			Object v = values[i];
			if (!c.isInstance(v)) {
				return false;
			}
		}
		return true;
	}

	public void invoke(Object instance) throws Throwable {
		try {
			method.invoke(instance, args);
		} catch (InvocationTargetException cause) {
			throw cause.getCause();
		}
	}

	@Override
	public String toString() {
		String methodName = method.getName();
		String argsValues = Stream.of(args)
				.map(arg -> arg.getClass().isArray() ? toArrayString(arg) : arg.toString())
				.collect(Collectors.joining(", "));
		return methodName + "(" + argsValues + ")";
	}

	private String toArrayString(Object arg) {
		try {
			// Add "array" prefix to differentiate it from collections
			return "array" + Arrays.class.getMethod("toString", arg.getClass()).invoke(null, arg);
		} catch (Exception cause) {
			throw new RuntimeException(cause);
		}
	}
}
