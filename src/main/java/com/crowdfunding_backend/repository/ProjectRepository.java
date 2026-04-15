package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.Project;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  // List<Project> findByDueDateAfter(LocalDateTime now);
  List<Project> findByDeadlineAfter(LocalDateTime now);
  Optional<Project> findByTitleAndCreator_Id(String title, Long creatorId);
  Optional<Project> findByIdAndCreator_Id(Long projectId, Long creatorId);
  Page<Project> findByTitleContainingIgnoreCase(String title,
                                                Pageable pageable);
  @Query(
      "SELECT p FROM Project p WHERE p.deadline > :now AND p.currentAmount < p.goalAmount")
  List<Project>
  findActiveAndNotFundedProjects(@Param("now") LocalDateTime now);
}