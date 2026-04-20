package com.crowdfunding_backend.repository;

import com.crowdfunding_backend.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository
    extends JpaRepository<Conversation, Long> {

  Optional<Conversation> findByUser1IdAndUser2IdAndProjectId(Long user1Id,
                                                             Long user2Id,
                                                             Long projectId);

  List<Conversation> findByUser1IdOrUser2IdOrderByCreatedAtDesc(Long user1Id,
                                                                 Long user2Id);
}