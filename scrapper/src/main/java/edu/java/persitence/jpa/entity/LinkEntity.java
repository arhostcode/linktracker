package edu.java.persitence.jpa.entity;

import edu.java.domain.dto.Link;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter @Entity
@Table(name = "link")
@AllArgsConstructor
@NoArgsConstructor
public class LinkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private String description;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "last_checked_at")
    private OffsetDateTime lastCheckedAt;

    @Column(name = "meta_information")
    private String metaInformation;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "links")
    private Set<TgChatEntity> tgChats = new HashSet<>();

    public LinkEntity(
        String url,
        String description,
        OffsetDateTime updatedAt,
        OffsetDateTime lastCheckedAt,
        String metaInformation
    ) {
        this.url = url;
        this.description = description;
        this.updatedAt = updatedAt;
        this.lastCheckedAt = lastCheckedAt;
        this.metaInformation = metaInformation;
    }

    public Link toDto() {
        return new Link(
            id,
            url,
            description,
            updatedAt,
            lastCheckedAt,
            metaInformation
        );
    }
}
