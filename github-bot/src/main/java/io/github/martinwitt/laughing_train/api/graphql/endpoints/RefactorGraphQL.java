package io.github.martinwitt.laughing_train.api.graphql.endpoints;

import com.google.common.flogger.FluentLogger;
import io.github.martinwitt.laughing_train.persistence.repository.BadSmellRepository;
import io.github.martinwitt.laughing_train.services.RefactorService;
import io.quarkus.security.Authenticated;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import xyz.keksdose.spoon.code_solver.analyzer.AnalyzerRule;
import xyz.keksdose.spoon.code_solver.analyzer.qodana.QodanaRules;

@GraphQLApi
@RequestScoped
public class RefactorGraphQL {
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Inject
    RefactorService refactorService;

    @Inject
    BadSmellRepository badSmellRepository;

    @Query
    @Description("Returns a list of all available refactorings")
    public List<? extends AnalyzerRule> getAvailableRefactorings() {
        return Arrays.stream(QodanaRules.values()).toList();
    }

    @Mutation
    @Description("Refactoring the given bad smells")
    @Authenticated
    public String refactor(List<String> badSmellIdentifier) {
        badSmellIdentifier.stream().map(badSmellRepository::findByIdentifier).forEach(badSmell -> {
            logger.atInfo().log("Refactoring %s", badSmell);
            refactorService.refactor(badSmell);
        });
        return "Refactoring done";
    }
}
