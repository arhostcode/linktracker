package edu.java.persitence.common.repository;

import edu.java.domain.dto.Link;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {

    List<Link> findAll();

    Long add(Link link);

    Long remove(long linkId);

    Optional<Link> findById(long linkId);

    Optional<Link> findByUrl(String url);

    List<Link> findLinksCheckedAfter(Duration after, int limit);

    void update(long id, OffsetDateTime lastModified, String metaInformation);

    void checkNow(long id);
}
