package io.github.martinwitt.laughing_train.persistence.impl;

import com.google.common.flogger.FluentLogger;
import com.mongodb.client.model.Filters;
import io.github.martinwitt.laughing_train.domain.value.RuleId;
import io.github.martinwitt.laughing_train.persistence.BadSmell;
import io.github.martinwitt.laughing_train.persistence.converter.BadSmellDaoConverter;
import io.github.martinwitt.laughing_train.persistence.dao.BadSmellDao;
import io.github.martinwitt.laughing_train.persistence.repository.BadSmellRepository;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.bson.conversions.Bson;

@ApplicationScoped
public class MongoBadSmellRepository implements BadSmellRepository, PanacheMongoRepository<BadSmellDao> {

    private static final BadSmellDaoConverter badSmellConverter = new BadSmellDaoConverter();
    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    public List<BadSmell> findByRuleID(RuleId ruleID) {
        return find("ruleID", ruleID.id()).stream()
                .map(badSmellConverter::convertToEntity)
                .toList();
    }

    public List<BadSmell> findByProjectName(String projectName) {
        return find("projectName", projectName).stream()
                .map(badSmellConverter::convertToEntity)
                .toList();
    }

    public List<BadSmell> findByProjectUrl(String projectUrl) {
        return find("projectUrl", projectUrl).stream()
                .map(badSmellConverter::convertToEntity)
                .toList();
    }

    public List<BadSmell> findByCommitHash(String commitHash) {
        return find("commitHash", commitHash).stream()
                .map(badSmellConverter::convertToEntity)
                .toList();
    }

    public List<BadSmell> findByIdentifier(String identifier) {
        return find("identifier", identifier).stream()
                .map(badSmellConverter::convertToEntity)
                .toList();
    }

    @Override
    public long deleteByIdentifier(String identifier) {
        return delete("identifier", identifier);
    }

    @Override
    public BadSmell save(BadSmell badSmell) {
        var list = find("identifier", badSmell.getIdentifier()).list();
        if (list.isEmpty()) {
            persist(badSmellConverter.convertToDao(badSmell));
        }
        return badSmell;
    }

    @Override
    public Stream<BadSmell> getAll() {
        return streamAll().map(badSmellConverter::convertToEntity);
    }

    @Override
    public List<BadSmell> findByCommitHash(String commitHash, String analyzerName) {
        Bson filter = Filters.and(Filters.eq("commitHash", commitHash), Filters.eq("analyzer", analyzerName));
        return StreamSupport.stream(mongoCollection().find(filter).spliterator(), false)
                .map(badSmellConverter::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BadSmell> findByCommitHash(String commitHash, String analyzerName, String ruleId) {
        Bson filter = Filters.and(
                Filters.eq("commitHash", commitHash),
                Filters.eq("analyzer", analyzerName),
                Filters.eq("ruleID", ruleId));
        return StreamSupport.stream(mongoCollection().find(filter).spliterator(), false)
                .map(badSmellConverter::convertToEntity)
                .collect(Collectors.toList());
    }
}
