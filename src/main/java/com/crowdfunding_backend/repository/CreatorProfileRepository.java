package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.CreatorProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorProfileRepository
    extends JpaRepository<CreatorProfile, Long> {

  Optional<CreatorProfile> findByUser_Id(Long userId);
}
