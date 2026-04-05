package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  Optional<Project> findByTitleAndCreator_Id(String title, Long creatorId);
  Optional<Project> findByIdAndCreator_Id(Long projectId, Long creatorId);
}