package fr.vergne.pester.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fr.vergne.pester.value.Type;

public class TypeFactory {

	private interface ClassTypeFactory {
		<T> ClassType<T> createType(Class<T> typeClass);
	}

	private final Map<Class<?>, ClassType<?>> classTypes = new HashMap<>();
	private ClassTypeFactory classTypeFactory = new ClassTypeFactory() {

		@Override
		public <T> ClassType<T> createType(Class<T> typeClass) {
			return new ClassType<>(typeClass);
		}
	};
	
	// Package-scope constructor
	TypeFactory() {
	}

	public interface Extension {
		<T> void extendType(Class<T> typeClass, Type<T> type);
	}

	public void addExtension(Extension extension) {
		ClassTypeFactory oldFactory = classTypeFactory;
		classTypeFactory = new ClassTypeFactory() {

			@Override
			public <T> ClassType<T> createType(Class<T> typeClass) {
				ClassType<T> type = oldFactory.createType(typeClass);
				extension.extendType(typeClass, type);
				return type;
			}
		};
	}

	@SuppressWarnings("unchecked")
	public <T> Type<T> from(Class<T> typeClass) {
		return (Type<T>) classTypes.computeIfAbsent(typeClass, t -> classTypeFactory.createType(t));
	}

	public <T> Type<T> as(String name) {
		return new NamedType<>(name, Optional.empty());
	}

	public <T> Type<T> as(String name, Class<?> mainClass) {
		return new NamedType<>(name, Optional.of(mainClass));
	}
}
