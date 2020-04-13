package fr.vergne.pester.junit.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import fr.vergne.pester.PesterTest;
import fr.vergne.pester.definition.PojoDefinition;
import fr.vergne.pester.util.cache.Cache;

class ExtensionCache {

	private static final Namespace NAMESPACE = Namespace.create(new Object());
	
	private final Store store;

	public ExtensionCache(ExtensionContext context) {
		this.store = context.getRoot().getStore(NAMESPACE);
	}

	public PojoDefinition<?> getPojoDefinition(PesterTest<?> testInstance) {
		return store.getOrComputeIfAbsent(
						testInstance,
						PesterTest::createPojoDefinition,
						PojoDefinition.class);
	}

	public Cache getTestCache(String contextId) {
		return store.getOrComputeIfAbsent(
				"testCache" + contextId,
				idKey -> Cache.create(),
				Cache.class);
	}
}
