package fr.vergne.pester.junit.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

import fr.vergne.pester.junit.extension.EmptyParameterizedDefinitionTestsDisabler;

/**
 * This annotation applies the
 * {@link EmptyParameterizedDefinitionTestsDisabler} to all the
 * {@link ParameterizedTest}s of a given test class.
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(EmptyParameterizedDefinitionTestsDisabler.class)
public @interface DisableParameterizedTestsWithNoCase {
}
