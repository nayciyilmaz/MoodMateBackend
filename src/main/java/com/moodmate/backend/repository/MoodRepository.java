package com.moodmate.backend.repository;

import com.moodmate.backend.entity.Mood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MoodRepository extends JpaRepository<Mood, Long> {
    List<Mood> findAllByUserIdOrderByEntryDateDesc(Long userId);
}