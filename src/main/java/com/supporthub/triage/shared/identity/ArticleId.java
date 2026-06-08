package com.supporthub.triage.shared.identity;

import com.supporthub.triage.shared.error.BusinessValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Identity of a projected knowledge article. Mapped as an {@code @Embeddable} so the domain works
 * with the value object while the database stores the raw UUID column (Hibernate forbids attribute
 * converters on identifier attributes, so id value objects use the embeddable component mapping).
 */
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JPA
public final class ArticleId {

    @Column(name = "article_id")
    private UUID value;

    private ArticleId(UUID value) {
        if (value == null) {
            throw new BusinessValidationException("Invalid article id");
        }
        this.value = value;
    }

    public static ArticleId of(UUID value) {
        return new ArticleId(value);
    }

    UUID unwrap() {
        return value;
    }

    public boolean sameAs(ArticleId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ArticleId[" + value + "]";
    }
}
