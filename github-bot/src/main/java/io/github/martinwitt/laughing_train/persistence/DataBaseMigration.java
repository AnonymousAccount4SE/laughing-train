package io.github.martinwitt.laughing_train.persistence;

import com.google.common.flogger.FluentLogger;
import com.mongodb.client.model.Filters;
import io.github.martinwitt.laughing_train.domain.entity.Project;
import io.github.martinwitt.laughing_train.domain.entity.ProjectConfig;
import io.github.martinwitt.laughing_train.persistence.impl.MongoBadSmellRepository;
import io.github.martinwitt.laughing_train.persistence.impl.MongoProjectRepository;
import io.github.martinwitt.laughing_train.persistence.repository.BadSmellRepository;
import io.github.martinwitt.laughing_train.persistence.repository.ProjectConfigRepository;
import io.github.martinwitt.laughing_train.persistence.repository.ProjectRepository;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class is used to migrate the database to the latest version.
 */
@ApplicationScoped
public class DataBaseMigration {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    @Inject
    ProjectConfigRepository projectConfigRepository;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    BadSmellRepository badSmellRepository;
    // we use this for faster mongodb access
    @Inject
    MongoBadSmellRepository badSmellRepositoryImpl;

    @Inject
    MongoProjectRepository projectRepositoryImpl;

    @Inject
    Vertx vertx;

    /**
     * This method is called by the quarkus framework to migrate the database.
     */
    public void onStart(@Observes StartupEvent event) {
        checkPeriodic();
    }

    public void checkPeriodic() {
        vertx.setPeriodic(
                TimeUnit.MINUTES.toMillis(2),
                TimeUnit.MINUTES.toMillis(60),
                id -> vertx.executeBlocking(promise -> migrateDataBase(promise)));
    }

    private void migrateDataBase(Promise<Object> promise) {
        logger.atInfo().log("Migrating database");
        createConfigsIfMissing();
        removeProjectHashesWithoutResults();
        removeProjectsWithOutHashes();
        removeDuplicatedProjects();
        logger.atInfo().log("Finished migrating database");
        promise.complete();
    }

    private void removeProjectsWithOutHashes() {
        logger.atInfo().log("Removing projects without commit hashes");
        long value = projectRepository.getAll().stream()
                .filter(project -> project.getCommitHashes().isEmpty())
                .map(project -> projectRepository.deleteByProjectUrl(project.getProjectUrl()))
                .count();
        logger.atInfo().log("Removed %d projects without commit hashes", value);
    }

    private void createConfigsIfMissing() {
        long value = projectRepository.getAll().stream()
                .map(Project::getProjectUrl)
                .filter(projectUrl ->
                        projectConfigRepository.findByProjectUrl(projectUrl).isEmpty())
                .map(v -> projectConfigRepository.create(ProjectConfig.ofProjectUrl(v)))
                .count();
        logger.atInfo().log("Created %d project configs", value);
    }

    private void removeProjectHashesWithoutResults() {
        logger.atInfo().log("Removing project hashes without results");
        for (Project project : projectRepository.getAll()) {
            List<String> commitHashes = project.getCommitHashes();
            for (String commitHash : commitHashes) {
                if (badSmellRepositoryImpl
                                .mongoCollection()
                                .find((Filters.eq("commitHash", commitHash)))
                                .first()
                        == null) {
                    project.removeCommitHash(commitHash);
                }
            }
            projectRepository.deleteByProjectUrl(project.getProjectUrl());
            projectRepository.save(project);
        }
        logger.atInfo().log("Finished removing project hashes without results");
    }

    private void removeDuplicatedProjects() {
        logger.atInfo().log("Removing duplicated projects");
        projectRepository.getAll().stream().collect(Collectors.groupingBy(Project::getProjectUrl)).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .peek(entry -> logger.atInfo().log(
                        "Found %d projects with url %s", entry.getValue().size(), entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .forEach(project -> projectRepository.deleteByProjectUrl(project.getProjectUrl()));
        logger.atInfo().log("Finished removing duplicated projects");
    }
}
