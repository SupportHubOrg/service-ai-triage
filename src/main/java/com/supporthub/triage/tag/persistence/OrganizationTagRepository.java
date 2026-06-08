package com.supporthub.triage.tag.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationTagRepository extends JpaRepository<OrganizationTagEntity, UUID> {

    List<OrganizationTagEntity> findByOrganizationIdAndDeletedFalseAndExcludedFromAiFalseAndNameIsNotNullOrderByNameAsc(
            UUID organizationId
    );
}
