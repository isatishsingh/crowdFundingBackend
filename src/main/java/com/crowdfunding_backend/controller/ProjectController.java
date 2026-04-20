package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.project.*;
import com.crowdfunding_backend.service.ProjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

  @Autowired private ProjectService projectService;

  @PostMapping
  @PreAuthorize("hasRole('CREATOR')")
  public ProjectResponse createProject(@RequestBody ProjectRequest request,
                                       Authentication authentication) {

    String email = authentication.getName();
    return projectService.createProject(request, email);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('CREATOR')")
  public ProjectResponse updateProject(@PathVariable Long id,
                                       @RequestBody ProjectRequest request,
                                       Authentication authentication) {

    return projectService.updateProject(id, request, authentication.getName());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('CREATOR')")
  public String deleteProject(@PathVariable Long id,
                              Authentication authentication) {

    return projectService.deleteProject(id, authentication.getName());
  }

  @GetMapping
  public List<ProjectListResponse> getAllProjects() {
    return projectService.getAllProjects();
  }

  /** Completed investments: investor name, amount, equity %, date (public). */
  @GetMapping("/{id}/investors")
  public List<ProjectInvestorResponse> getProjectInvestors(@PathVariable Long id) {
    return projectService.getProjectInvestors(id);
  }

  @GetMapping("/{id}")
  public ProjectResponse getProject(@PathVariable Long id) {
    return projectService.getProject(id);
  }

  @GetMapping("/{id}/stats")
  public ProjectStatsResponse getProjectStats(@PathVariable Long id) {
    return projectService.getProjectStats(id);
  }
}