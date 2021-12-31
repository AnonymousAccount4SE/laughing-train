
package xyz.keksdose.spoon.code_solver.transformations.junit;

import java.util.Optional;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import xyz.keksdose.spoon.code_solver.history.Change;
import xyz.keksdose.spoon.code_solver.history.ChangeListener;
import xyz.keksdose.spoon.code_solver.transformations.TransformationProcessor;

public class Junit4AnnotationsTransformation extends TransformationProcessor<CtMethod<?>> {

	public Junit4AnnotationsTransformation(ChangeListener listener) {
		super(listener);
	}

	@Override
	public void process(CtMethod<?> method) {
		refactorBeforeClass(method);
		refactorBefore(method);
		refactorAfter(method);
		refactorAfterClass(method);
		refactorIgnore(method);
	}

	private void refactorBeforeClass(CtMethod<?> method) {
		Optional<CtAnnotation<?>> beforeClassAnnotation = JunitHelper.getJunit4BeforeClassAnnotation(method);
		if (beforeClassAnnotation.isPresent()) {
			method.removeAnnotation(beforeClassAnnotation.get());
			method.addAnnotation(JunitHelper.createBeforeAllAnnotation(getFactory()));
			setChanged(method.getParent(CtType.class),
				new Change(String.format("Replaced @BeforeClass annotation with @BeforeAll from method %s",
					method.getSimpleName()), "Junit4AnnotationsTransformation", method.getParent(CtType.class)));
		}
	}

	private void refactorBefore(CtMethod<?> method) {
		Optional<CtAnnotation<?>> beforeAnnotation = JunitHelper.getJunit4BeforeAnnotation(method);
		if (beforeAnnotation.isPresent()) {
			method.removeAnnotation(beforeAnnotation.get());
			method.addAnnotation(JunitHelper.createBeforeEachAnnotation(getFactory()));
			setChanged(method.getParent(CtType.class), new Change(
				String.format("Replaced @Before annotation with @BeforeEach from method %s", method.getSimpleName()),
				"Junit4AnnotationsTransformation", method.getParent(CtType.class)));
		}
	}

	private void refactorAfter(CtMethod<?> method) {
		Optional<CtAnnotation<?>> afterAnnotation = JunitHelper.getJunit4AfterAnnotation(method);
		if (afterAnnotation.isPresent()) {
			method.removeAnnotation(afterAnnotation.get());
			method.addAnnotation(JunitHelper.createAfterEachAnnotation(getFactory()));
			setChanged(method.getParent(CtType.class),
				new Change(
					String.format("Replaced @After annotation with @AfterEach from method %s", method.getSimpleName()),
					"Junit4AnnotationsTransformation", method.getParent(CtType.class)));
		}
	}

	private void refactorAfterClass(CtMethod<?> method) {
		Optional<CtAnnotation<?>> afterAnnotation = JunitHelper.getJunit4AfterClassAnnotation(method);
		if (afterAnnotation.isPresent()) {
			method.removeAnnotation(afterAnnotation.get());
			method.addAnnotation(JunitHelper.createAfterAllAnnotation(getFactory()));
			setChanged(method.getParent(CtType.class), new Change(
				String.format("Replaced @AfterClass annotation with @AfterAll from method %s", method.getSimpleName()),
				"Junit4AnnotationsTransformation", method.getParent(CtType.class)));
		}
	}

	private void refactorIgnore(CtMethod<?> method) {
		Optional<CtAnnotation<?>> ignoreAnnotation = JunitHelper.getIgnoreAnnotation(method);
		if (ignoreAnnotation.isPresent()) {
			method.removeAnnotation(ignoreAnnotation.get());
			method.addAnnotation(JunitHelper.createDisableAnnotation(getFactory()));
			setChanged(method.getParent(CtType.class),
				new Change(
					String.format("Replaced @Ignore annotation with @Disabled from method %s", method.getSimpleName()),
					"Junit4AnnotationsTransformation", method.getParent(CtType.class)));
		}
	}
}
