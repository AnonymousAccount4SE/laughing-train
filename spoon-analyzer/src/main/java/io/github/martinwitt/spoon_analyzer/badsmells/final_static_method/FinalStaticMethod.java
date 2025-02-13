package io.github.martinwitt.spoon_analyzer.badsmells.final_static_method;

import io.github.martinwitt.spoon_analyzer.BadSmell;
import io.github.martinwitt.spoon_analyzer.BadSmellVisitor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

public class FinalStaticMethod implements BadSmell {

    private static final String NAME = "FinalStaticMethod";
    private static final String description =
            "A final method is a method that cannot be overridden in a subclass. As static methods are bound to the class the cant be overridden only hidden.";
    private final CtMethod<?> method;
    private final CtType<?> affectedType;

    public FinalStaticMethod(CtMethod<?> method, CtType<?> affectedType) {
        this.method = method;
        this.affectedType = affectedType;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public CtType<?> getAffectedType() {
        return affectedType;
    }

    /**
     * @return the final static method
     */
    public CtMethod<?> getMethod() {
        return method;
    }

    @Override
    public <T> T accept(BadSmellVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
