package com.moodmate.backend.repository;

import com.moodmate.backend.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdviceRepository extends JpaRepository<Advice, Long> {
    Optional<Advice> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}