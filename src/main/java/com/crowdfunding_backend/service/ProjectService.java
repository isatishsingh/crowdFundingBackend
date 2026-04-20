package com.crowdfunding_backend.service;
// import static org.junit.jupiter.api.DynamicTest.stream;

import com.crowdfunding_backend.dto.project.*;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  @Autowired private ProjectRepository projectRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CreatorProfileRepository creatorProfileRepository;
  @Autowired private InvestmentRepository investmentRepository;

  public ProjectResponse createProject(ProjectRequest request, String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    // 🔥 KYC CHECK
    CreatorProfile profile =
        creatorProfileRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new RuntimeException("Complete KYC first"));

    if (!profile.getIsKycVerified()) {
      throw new RuntimeException("KYC not verified");
    }

    Optional<Project> existingProject =
        projectRepository.findByTitleAndCreator_Id(request.getTitle(),
                                                   user.getId());

    if (existingProject.isPresent()) {
      throw new RuntimeException("Project with same title already exists");
    }

    Project project = new Project();
    project.setTitle(request.getTitle());
    project.setDescription(request.getDescription());
    project.setGoalAmount(request.getGoalAmount());
    project.setCurrentAmount(0.0);
    project.setTotalEquityOffered(request.getTotalEquityOffered());
    project.setEquityAllocated(0.0);
    project.setDeadline(request.getDeadline());
    project.setCreatedAt(LocalDateTime.now());
    project.setCreator(user);

    projectRepository.save(project);

    return new ProjectResponse(project.getId(), project.getTitle(),
                               project.getGoalAmount(),
                               project.getCurrentAmount(), user.getEmail());
  }

  //   update project
  public ProjectResponse updateProject(Long id, ProjectRequest request,
                                       String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    Project project =
        projectRepository.findByIdAndCreator_Id(id, user.getId())
            .orElseThrow(()
                             -> new RuntimeException(
                                 "Unauthorized or project not found"));

    project.setTitle(request.getTitle());
    project.setDescription(request.getDescription());
    project.setGoalAmount(request.getGoalAmount());
    project.setDeadline(request.getDeadline());

    projectRepository.save(project);

    return new ProjectResponse(project.getId(), project.getTitle(),
                               project.getGoalAmount(),
                               project.getCurrentAmount(), user.getEmail());
  }

  //   delete project
  public String deleteProject(Long id, String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    Project project =
        projectRepository.findByIdAndCreator_Id(id, user.getId())
            .orElseThrow(()
                             -> new RuntimeException(
                                 "Unauthorized or project not found"));

    projectRepository.delete(project);

    return "Project deleted successfully";
  }

  public List<ProjectListResponse> getAllProjects() {

    // Best practice: only active + not fully funded
    List<Project> projects =
        projectRepository.findActiveAndNotFundedProjects(LocalDateTime.now());
    // .stream()
    // .filter(p -> p.getCurrentAmount() < p.getGoalAmount())
    // .collect(Collectors.toList());

    return projects.stream()
        .map(project -> {
          double percentage =
              project.getGoalAmount() != 0
                  ? (project.getCurrentAmount() / project.getGoalAmount()) * 100
                  : 0;

          double totalEquity = project.getTotalEquityOffered() != null
                                   ? project.getTotalEquityOffered()
                                   : 0.0;

          double allocatedEquity = project.getEquityAllocated() != null
                                       ? project.getEquityAllocated()
                                       : 0.0;

          double remainingEquity = totalEquity - allocatedEquity;

          double goal =
              project.getGoalAmount() != null ? project.getGoalAmount() : 0;

          double current = project.getCurrentAmount() != null
                               ? project.getCurrentAmount()
                               : 0;

          return ProjectListResponse.builder()
              .id(project.getId())
              .title(project.getTitle())
              .description(project.getDescription())
              .goalAmount(goal)
              .currentAmount(current)
              .deadline(project.getDeadline())
              .creatorId(project.getCreator().getId())
              .fundingPercentage(percentage)
              .totalEquityOffered(totalEquity)
              .equityAllocated(allocatedEquity)
              .remainingEquity(remainingEquity)
              .build();
        })
        .collect(Collectors.toList());
  }

  public ProjectResponse getProject(Long id) {

    Project project = projectRepository.findById(id).orElseThrow(
        () -> new RuntimeException("Project not found"));

    ProjectResponse dto = new ProjectResponse();

    dto.setId(project.getId());
    dto.setTitle(project.getTitle());

    dto.setGoalAmount(project.getGoalAmount());
    dto.setCurrentAmount(project.getCurrentAmount());

    // Remaining Amount
    dto.setRemainingAmount(project.getGoalAmount() -
                           project.getCurrentAmount());

    dto.setCreatorEmail(project.getCreator().getEmail());

    // Equity fields
    dto.setTotalEquityOffered(project.getTotalEquityOffered());
    dto.setEquityAllocated(project.getEquityAllocated());

    dto.setRemainingEquity(project.getTotalEquityOffered() -
                           project.getEquityAllocated());

    return dto;
  }

  public List<Project> getActiveProjects() {
    return projectRepository.findByDeadlineAfter(LocalDateTime.now());
  }

  /**
   * Completed investments (after payment) for transparency on the project
   * page.
   */
  public List<ProjectInvestorResponse> getProjectInvestors(Long projectId) {

    if (!projectRepository.existsById(projectId)) {
      throw new RuntimeException("Project not found");
    }

    return investmentRepository.findByProject_Id(projectId)
        .stream()
        .map(inv
             -> new ProjectInvestorResponse(
                 inv.getInvestor().getName(), inv.getAmount(),
                 inv.getEquityPercentage(), inv.getInvestedAt()))
        .collect(Collectors.toList());
  }

  public ProjectStatsResponse getProjectStats(Long projectId) {
    if (!projectRepository.existsById(projectId)) {
      throw new RuntimeException("Project not found");
    }

    long investorCount = investmentRepository.countByProject_Id(projectId);

    // Placeholder engagement counters until dedicated tables/endpoints are
    // implemented.
    return new ProjectStatsResponse(investorCount, 0, 0, 0);
  }
}