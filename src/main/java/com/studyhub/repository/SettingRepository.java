package com.studyhub.repository;

import com.studyhub.enums.SettingStatus;
import com.studyhub.model.Setting;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {

    @EntityGraph(attributePaths = "type")
    List<Setting> findAll(Specification<Setting> specification, Sort sort);

    @EntityGraph(attributePaths = "type")
    List<Setting> findByTypeIsNullOrderByNameAsc();

    @EntityGraph(attributePaths = "type")
    List<Setting> findByTypeIsNullAndStatusOrderByNameAsc(SettingStatus status);

    boolean existsByType_IdAndNameIgnoreCase(Long typeId, String name);

    boolean existsByType_IdAndNameIgnoreCaseAndIdNot(Long typeId, String name, Long id);

    @EntityGraph(attributePaths = "type")
    Optional<Setting> findWithTypeById(Long id);

    long countByType_Name(String typeName);

    List<Setting> findByType_Name(String typeName);

    @Query("""
           SELECT s
           FROM Setting s
           JOIN FETCH s.type t
           WHERE t.name = 'Course Category'
           """)
    List<Setting> findAllCategoriesFetch();
}
