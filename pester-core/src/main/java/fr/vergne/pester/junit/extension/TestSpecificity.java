package fr.vergne.pester.junit.extension;

import java.util.Comparator;

import org.junit.jupiter.api.MethodDescriptor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;

import fr.vergne.pester.junit.PesterTestComparator;

public class TestSpecificity implements MethodOrderer {

	private final Comparator<MethodDescriptor> descriptorComparator = Comparator.comparing(MethodDescriptor::getMethod,
			new PesterTestComparator());

	@Override
	public void orderMethods(MethodOrdererContext context) {
		context.getMethodDescriptors().sort(descriptorComparator);
	}
}
