package xyz.keksdose.spoon.code_solver.transformations.junit.simplification;

import java.util.List;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import xyz.keksdose.spoon.code_solver.history.Change;
import xyz.keksdose.spoon.code_solver.history.ChangeListener;
import xyz.keksdose.spoon.code_solver.transformations.ImportHelper;
import xyz.keksdose.spoon.code_solver.transformations.TransformationProcessor;
import xyz.keksdose.spoon.code_solver.transformations.junit.JunitHelper;

public class AssertFalseEqualsCheck extends TransformationProcessor<CtInvocation<?>> {

    public AssertFalseEqualsCheck(ChangeListener listener) {
        super(listener);
    }

    @Override
    public void process(CtInvocation<?> invocation) {
        if (invocation.getExecutable() != null && JunitHelper.isJunit5AssertFalse(invocation.getExecutable())) {
            CtInvocation<?> junit5AssertTrue = invocation;
            CtExpression<?> expression = invocation.getArguments().iterator().next();
            if (expression instanceof CtInvocation) {
                CtInvocation<?> equalsInvocation = (CtInvocation<?>) expression;
                if (equalsInvocation.getExecutable().getSimpleName().equals("equals")) {
                    CtExpression<?> firstArgument = equalsInvocation.getTarget();
                    CtExpression<?> secondArgument =
                            equalsInvocation.getArguments().iterator().next();
                    CtInvocation<?> junit5AssertEquals = createJunit5AssertNotEquals(firstArgument, secondArgument);
                    junit5AssertEquals.setComments(invocation.getComments());
                    junit5AssertTrue.replace(junit5AssertEquals);
                    if (invocation.getArguments().size() == 2) {
                        // readd the String if it fails argument
                        junit5AssertEquals.addArgument(
                                invocation.getArguments().get(1).clone());
                    }
                    adjustImports(invocation);
                    notifyChangeListener(junit5AssertEquals);
                }
            }
        }
    }

    private void adjustImports(CtInvocation<?> element) {
        CtType<?> parent = element.getParent(CtType.class);
        CtCompilationUnit compilationUnit = element.getPosition().getCompilationUnit();

        if (parent != null && !hasJunit5AssertFalseLeft(parent)) {
            ImportHelper.removeImport("org.junit.jupiter.api.Assertions.assertFalse", true, compilationUnit);
        }
        ImportHelper.addImport("org.junit.jupiter.api.Assertions.assertNotEquals", true, compilationUnit);
    }

    private boolean hasJunit5AssertFalseLeft(CtType<?> parent) {
        return parent.getElements(new TypeFilter<>(CtInvocation.class)).stream()
                .filter(v -> v.getExecutable() != null)
                .anyMatch(v -> JunitHelper.isJunit5AssertFalse(v.getExecutable()));
    }

    private CtInvocation<?> createJunit5AssertNotEquals(CtExpression<?> firstArgument, CtExpression<?> secondArgument) {
        CtTypeReference<?> typeRef = getFactory().Type().createReference("org.junit.jupiter.api.Assertions");
        CtTypeReference<?> voidType = getFactory().Type().voidPrimitiveType();
        CtTypeReference<Object> objectType = getFactory().Type().objectType();
        CtExecutableReference<?> assertEquals = getFactory()
                .Executable()
                .createReference(typeRef, voidType, "assertNotEquals", List.of(objectType, objectType));
        return getFactory().createInvocation(null, assertEquals, List.of(firstArgument, secondArgument));
    }

    private void notifyChangeListener(CtInvocation<?> newAssert) {
        CtType<?> parent = newAssert.getParent(CtType.class);
        setChanged(
                parent,
                new Change(
                        String.format("Replaced assertFalse checking equals with assertNotEquals"),
                        "assertFalse with equals instead of assertNotEquals",
                        parent));
    }
}
