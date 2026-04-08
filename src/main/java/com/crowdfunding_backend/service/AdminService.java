package com.crowdfunding_backend.service;

import com.crowdfunding_backend.dto.admin.AdminDashboardResponse;
import com.crowdfunding_backend.entity.Project;
import com.crowdfunding_backend.entity.User;
import com.crowdfunding_backend.repository.InvestmentRepository;
import com.crowdfunding_backend.repository.ProjectRepository;
import com.crowdfunding_backend.repository.UserRepository;
import com.crowdfunding_backend.service.AdminService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

  @Autowired private UserRepository userRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private InvestmentRepository investmentRepository;

  public AdminDashboardResponse getDashboardData() {

    AdminDashboardResponse response = new AdminDashboardResponse();

    response.setTotalUsers(userRepository.count());
    response.setTotalProjects(projectRepository.count());
    response.setTotalInvestments(investmentRepository.count());

    // total funding calculation
    double totalFunding = investmentRepository.findAll()
                              .stream()
                              .mapToDouble(investment -> investment.getAmount())
                              .sum();

    response.setTotalFunding(totalFunding);

    return response;
  }

  public List<User> getAllUsers() { return userRepository.findAll(); }

  public List<Project> getAllProjects() { return projectRepository.findAll(); }

  public void deleteUser(Long id) { userRepository.deleteById(id); }

  public void deleteProject(Long id) { projectRepository.deleteById(id); }

  public Page<Project> getAllProjects(int page, int size, String search) {

    Pageable pageable = PageRequest.of(page, size);

    if (search != null && !search.isEmpty()) {
      return projectRepository.findByTitleContainingIgnoreCase(search,
                                                               pageable);
    }

    return projectRepository.findAll(pageable);
  }

  public Page<User> getAllUsers(int page, int size, String search) {

    Pageable pageable = PageRequest.of(page, size);

    if (search != null && !search.isEmpty()) {
      return userRepository.findByEmailContainingIgnoreCase(search, pageable);
    }

    return userRepository.findAll(pageable);
  }
}