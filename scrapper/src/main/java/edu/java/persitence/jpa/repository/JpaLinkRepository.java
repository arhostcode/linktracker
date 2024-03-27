package edu.java.persitence.jpa.repository;

import edu.java.persitence.jpa.entity.LinkEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLinkRepository extends JpaRepository<LinkEntity, Long> {
    List<LinkEntity> findAllByLastCheckedAtBefore(OffsetDateTime from, Limit limit);

    Optional<LinkEntity> findByUrl(String url);
}
