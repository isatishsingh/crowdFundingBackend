package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.dto.project.*;
import com.crowdfunding_backend.entity.User;
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

    System.out.println("Logged in user: " + authentication.getName());
    User user = (User)authentication.getPrincipal();
    String email = user.getEmail();
    System.out.println("Logged in email: " + email);
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

  /** Investor / public listing — open campaigns only. */
  @GetMapping
  public List<ProjectListResponse> getProjectsForInvestors() {
    return projectService.getProjectsForInvestors();
  }

  /** Creator dashboard — all own campaigns including fully funded. */
  @GetMapping("/mine")
  @PreAuthorize("hasRole('CREATOR')")
  public List<ProjectListResponse> getMyProjects(Authentication authentication) {
    User user = (User)authentication.getPrincipal();
    return projectService.getProjectsForCreator(user.getEmail());
  }

  /** Completed investments: investor name, amount, equity %, date (public). */
  @GetMapping("/{id}/investors")
  public List<ProjectInvestorResponse>
  getProjectInvestors(@PathVariable Long id) {
    return projectService.getProjectInvestors(id);
  }

  @GetMapping("/{id}")
  public ProjectResponse getProject(@PathVariable Long id, Authentication authentication) {
    User viewer = authentication != null ? (User)authentication.getPrincipal() : null;
    return projectService.getProject(id, viewer);
  }

  @GetMapping("/{id}/stats")
  public ProjectStatsResponse getProjectStats(@PathVariable Long id) {
    return projectService.getProjectStats(id);
  }
}