package io.github.martinwitt.laughing_train.services;

import com.google.common.base.Strings;
import com.google.common.flogger.FluentLogger;
import io.github.martinwitt.laughing_train.data.Project;
import io.github.martinwitt.laughing_train.data.ProjectRequest;
import io.github.martinwitt.laughing_train.data.ProjectResult;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;

@ApplicationScoped
public class ProjectService {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();
    private static final Random random = new Random();

    @Inject
    Vertx vertx;

    public ProjectResult handleProjectRequest(ProjectRequest request) {
        try {
            logger.atInfo().log("Received project request %s", request);
            if (request instanceof ProjectRequest.WithUrl url) {

                String repoName = StringUtils.substringAfterLast(url.url(), "/").replace(".git", "");
                Path dir = Files.createTempDirectory("laughing-train-" + repoName + random.nextLong());
                cleanAfter60min(dir);
                return checkoutRepo(url, dir)
                        .onItem()
                        .invoke(() -> logger.atInfo().log("Cloned %s to %s", url.url(), dir))
                        .onFailure()
                        .invoke(e -> logger.atSevere().withCause(e).log("Error while cloning %s to %s", url.url(), dir))
                        .onFailure()
                        .invoke(e -> FileUtils.deleteQuietly(dir.toFile()))
                        .onItemOrFailure()
                        .<ProjectResult>transform((git, error) -> toResult(url, repoName, dir, git, error))
                        .await()
                        .indefinitely();
            }
        } catch (Exception e) {
            logger.atSevere().withCause(e).log("Error while handling project request %s", request);
            return new ProjectResult.Error(e.getMessage());
        }
        return new ProjectResult.Error("Unknown request");
    }

    private void cleanAfter60min(Path dir) {
        vertx.setTimer(Duration.ofMinutes(60).toMillis(), v -> {
            if (Files.exists(dir)) {
                vertx.fileSystem().deleteRecursive(dir.toAbsolutePath().toString(), true);
            }
        });
    }

    private ProjectResult toResult(ProjectRequest.WithUrl url, String repoName, Path dir, Git git, Throwable error) {
        if (error == null) {
            String commitHash = getHash(git);
            return new ProjectResult.Success(new Project(repoName, url.url(), dir.toFile(), ".", commitHash));
        } else {
            git.close();
            return new ProjectResult.Error(Strings.nullToEmpty(error.getMessage()));
        }
    }

    private String getHash(Git git) {
        try {
            return ObjectId.toString(git.log().call().iterator().next().getId());
        } catch (GitAPIException e) {
            return "Error while getting hash";
        }
    }

    private Uni<Git> checkoutRepo(ProjectRequest.WithUrl url, Path dir) {
        return createAsyncRepo(url, dir);
    }

    private Uni<Git> createAsyncRepo(ProjectRequest.WithUrl url, Path dir) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            FileUtils.deleteDirectory(dir.toFile());
            Files.createDirectories(dir);
            return Git.cloneRepository()
                    .setURI(url.url())
                    .setDirectory(dir.toFile())
                    .call();
        }));
    }
}
