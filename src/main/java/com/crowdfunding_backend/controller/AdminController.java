package com.crowdfunding_backend.controller;

import com.crowdfunding_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

  @Autowired private AdminService adminService;

  @GetMapping("/dashboard")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> getDashboard() {
    return ResponseEntity.ok(adminService.getDashboardData());
  }

  //   @GetMapping("/users")
  //   @PreAuthorize("hasRole('ADMIN')")
  //   public ResponseEntity<?> getUsers() {
  //     return ResponseEntity.ok(adminService.getAllUsers());
  //   }

  @GetMapping("/projects")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?>
  getProjects(@RequestParam(defaultValue = "0") int page,
              @RequestParam(defaultValue = "5") int size,
              @RequestParam(required = false) String search) {
    return ResponseEntity.ok(adminService.getAllProjects(page, size, search));
  }

  @DeleteMapping("/user/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    adminService.deleteUser(id);
    return ResponseEntity.ok("User deleted");
  }

  @DeleteMapping("/project/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> deleteProject(@PathVariable Long id) {
    adminService.deleteProject(id);
    return ResponseEntity.ok("Project deleted");
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?>
  getUsers(@RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "5") int size,
           @RequestParam(required = false) String search) {

    return ResponseEntity.ok(adminService.getAllUsers(page, size, search));
  }
}