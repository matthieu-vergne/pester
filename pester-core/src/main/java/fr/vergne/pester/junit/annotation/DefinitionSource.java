package fr.vergne.pester.junit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.provider.ArgumentsSource;

import fr.vergne.pester.junit.TestParameter;
import fr.vergne.pester.junit.extension.DefinitionCasesProvider;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(DefinitionCasesProvider.class)
public @interface DefinitionSource {
	
    /**
     * The {@link TestParameter}s required by the test
     */
    TestParameter[] value();
}
