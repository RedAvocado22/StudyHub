package com.studyhub.repository;

import com.studyhub.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting, Long> {

    long countByType_Name(String typeName);

    List<Setting> findByType_Name(String typeName);
}
