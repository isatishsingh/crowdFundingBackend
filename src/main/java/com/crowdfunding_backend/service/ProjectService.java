package com.crowdfunding_backend.service;
import com.crowdfunding_backend.dto.project.*;
import com.crowdfunding_backend.entity.*;
import com.crowdfunding_backend.repository.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

  @Autowired private ProjectRepository projectRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CreatorProfileRepository creatorProfileRepository;

  public ProjectResponse createProject(ProjectRequest request, String email) {

    User user = userRepository.findByEmail(email).orElseThrow(
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

    User user = userRepository.findByEmail(email).orElseThrow(
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

    User user = userRepository.findByEmail(email).orElseThrow(
        () -> new RuntimeException("User not found"));

    Project project =
        projectRepository.findByIdAndCreator_Id(id, user.getId())
            .orElseThrow(()
                             -> new RuntimeException(
                                 "Unauthorized or project not found"));

    projectRepository.delete(project);

    return "Project deleted successfully";
  }
}