package com.supporthub.triage.tag.persistence;

import com.supporthub.triage.shared.identity.OrganizationId;
import com.supporthub.triage.shared.identity.TagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganizationTagRepository extends JpaRepository<OrganizationTagEntity, TagId> {

    List<OrganizationTagEntity> findByOrganizationIdAndDeletedFalseAndExcludedFromAiFalseAndNameIsNotNullOrderByNameAsc(
            OrganizationId organizationId
    );
}
