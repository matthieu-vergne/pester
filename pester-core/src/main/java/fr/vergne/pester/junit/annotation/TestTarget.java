package fr.vergne.pester.junit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fr.vergne.pester.junit.TestParameter;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestTarget {
	
    /**
     * The {@link TestParameter} targeted by the test
     */
    TestParameter value();
}
