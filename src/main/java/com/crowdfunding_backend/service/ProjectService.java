package com.crowdfunding_backend.service;
// import static org.junit.jupiter.api.DynamicTest.stream;

import com.crowdfunding_backend.dto.project.*;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.exception.CustomException;
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

  @Autowired private SubscriptionService subscriptionService;

  public ProjectResponse createProject(ProjectRequest request, String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    // check KYC and subscription status of creator before creating any project
    CreatorProfile profile =
        creatorProfileRepository.findByUser_Id(user.getId())
            .orElseThrow(
                ()
                    -> new CustomException(
                        "Complete creator verification (KYC) before listing a project.",
                        403, "KYC_NOT_SUBMITTED"));

    if (!profile.getIsKycVerified()) {
      throw new CustomException(
          "Your KYC is pending approval. You can create projects after verification.",
          403, "KYC_PENDING");
    }

    subscriptionService.validateCreatorCanCreateProject(
        user, request.getGoalAmount());

    Optional<Project> existingProject =
        projectRepository.findByTitleAndCreator_Id(request.getTitle(),
                                                   user.getId());

    if (existingProject.isPresent()) {
      throw new CustomException(
          "You already have a project with this title. Choose a different title.",
          400);
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

    return toProjectResponse(project);
  }

  //   update project
  public ProjectResponse updateProject(Long id, UpdateProjectRequest request,
                                       String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    Project project =
        projectRepository.findByIdAndCreator_Id(id, user.getId())
            .orElseThrow(
                ()
                    -> new CustomException(
                        "Project not found or you do not have permission to edit it.",
                        404));

    if (request.getDeadline() == null ||
        !request.getDeadline().isAfter(LocalDateTime.now())) {
      throw new CustomException(
          "Listing deadline must be a future date and time.", 400);
    }

    project.setTitle(request.getTitle().trim());
    project.setDescription(request.getDescription().trim());
    project.setDeadline(request.getDeadline());

    projectRepository.save(project);

    return toProjectResponse(project);
  }

  //   delete project
  public String deleteProject(Long id, String email) {

    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    Project project =
        projectRepository.findByIdAndCreator_Id(id, user.getId())
            .orElseThrow(
                ()
                    -> new CustomException(
                        "Project not found or you do not have permission to delete it.",
                        404));

    if (investmentRepository.countByProject_Id(id) > 0) {
      throw new CustomException(
          "This project cannot be deleted because it already has investments.",
          400);
    }

    double funded =
        project.getCurrentAmount() != null ? project.getCurrentAmount() : 0;
    if (funded > 0.01) {
      throw new CustomException(
          "This project cannot be deleted after funding has started.", 400);
    }

    projectRepository.delete(project);

    return "Project deleted successfully";
  }

  /**
   * Investor / public browse — excludes fully funded or equity-exhausted
   * campaigns.
   */
  public List<ProjectListResponse> getProjectsForInvestors() {
    List<Project> projects =
        projectRepository.findOpenForInvestors(LocalDateTime.now());
    return projects.stream()
        .map(this::toListResponse)
        .collect(Collectors.toList());
  }

  /** Creator dashboard — includes fully funded projects. */
  public List<ProjectListResponse> getProjectsForCreator(String email) {
    User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(
        () -> new CustomException("User account not found.", 404));

    return projectRepository.findByCreator_IdOrderByCreatedAtDesc(user.getId())
        .stream()
        .map(this::toListResponse)
        .collect(Collectors.toList());
  }

  private ProjectListResponse toListResponse(Project project) {
    LocalDateTime now = LocalDateTime.now();
    boolean open = isOpenForInvestment(project, now);
    String listingStatus = resolveListingStatus(project, now, open);

    double goal = project.getGoalAmount() != null ? project.getGoalAmount() : 0;
    double current =
        project.getCurrentAmount() != null ? project.getCurrentAmount() : 0;
    double percentage = goal != 0 ? (current / goal) * 100 : 0;

    double totalEquity = project.getTotalEquityOffered() != null
                             ? project.getTotalEquityOffered()
                             : 0.0;
    double allocatedEquity = project.getEquityAllocated() != null
                                 ? project.getEquityAllocated()
                                 : 0.0;

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
        .remainingEquity(totalEquity - allocatedEquity)
        .openForInvestment(open)
        .listingStatus(listingStatus)
        .build();
  }

  private boolean isOpenForInvestment(Project project, LocalDateTime now) {
    if (project.getDeadline() == null || !project.getDeadline().isAfter(now)) {
      return false;
    }

    double goal = project.getGoalAmount() != null ? project.getGoalAmount() : 0;
    double current =
        project.getCurrentAmount() != null ? project.getCurrentAmount() : 0;
    if (goal > 0 && current >= goal - 0.01) {
      return false;
    }

    double totalEquity = project.getTotalEquityOffered() != null
                             ? project.getTotalEquityOffered()
                             : 0;
    double allocated =
        project.getEquityAllocated() != null ? project.getEquityAllocated() : 0;
    if (totalEquity > 0 && allocated >= totalEquity - 0.01) {
      return false;
    }

    return true;
  }

  private String resolveListingStatus(Project project, LocalDateTime now,
                                      boolean open) {
    if (project.getDeadline() != null && !project.getDeadline().isAfter(now)) {
      return "EXPIRED";
    }
    if (!open) {
      return "FUNDED";
    }
    return "ACTIVE";
  }

  public ProjectResponse getProject(Long id, User viewer) {

    Project project = projectRepository.findById(id).orElseThrow(
        () -> new CustomException("Project not found.", 404));

    boolean open = isOpenForInvestment(project, LocalDateTime.now());
    boolean isOwner = viewer != null && project.getCreator() != null &&
                      viewer.getId().equals(project.getCreator().getId());

    if (!open && viewer != null && viewer.getRole() == Role.INVESTOR) {
      throw new CustomException(
          "This project has reached its funding goal and is no longer listed for investors.",
          404);
    }

    if (!open && viewer == null) {
      throw new CustomException(
          "This project is no longer open for investment.", 404);
    }

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

    dto.setDescription(project.getDescription());
    dto.setDeadline(project.getDeadline());

    applyCreatorGstForInvestor(dto, project, viewer);

    return dto;
  }

  private void applyCreatorGstForInvestor(ProjectResponse dto, Project project,
                                        User viewer) {
    if (viewer == null || viewer.getRole() != Role.INVESTOR ||
        project.getCreator() == null) {
      return;
    }

    creatorProfileRepository
        .findByUser_Id(project.getCreator().getId())
        .map(CreatorProfile::getGstNumber)
        .filter(gst -> gst != null && !gst.isBlank())
        .ifPresent(dto::setCreatorGstNumber);
  }

  private ProjectResponse toProjectResponse(Project project) {
    ProjectResponse dto = new ProjectResponse();
    dto.setId(project.getId());
    dto.setTitle(project.getTitle());
    dto.setDescription(project.getDescription());
    dto.setGoalAmount(project.getGoalAmount());
    dto.setCurrentAmount(project.getCurrentAmount());
    dto.setDeadline(project.getDeadline());
    dto.setCreatorEmail(
        project.getCreator() != null ? project.getCreator().getEmail() : null);
    dto.setTotalEquityOffered(project.getTotalEquityOffered());
    dto.setEquityAllocated(project.getEquityAllocated());
    double goal = project.getGoalAmount() != null ? project.getGoalAmount() : 0;
    double current =
        project.getCurrentAmount() != null ? project.getCurrentAmount() : 0;
    dto.setRemainingAmount(goal - current);
    double totalEquity = project.getTotalEquityOffered() != null
                             ? project.getTotalEquityOffered()
                             : 0;
    double allocated = project.getEquityAllocated() != null
                         ? project.getEquityAllocated()
                         : 0;
    dto.setRemainingEquity(totalEquity - allocated);
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