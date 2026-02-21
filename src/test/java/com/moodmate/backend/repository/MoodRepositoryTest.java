package com.moodmate.backend.repository;

import com.moodmate.backend.entity.Mood;
import com.moodmate.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MoodRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MoodRepository moodRepository;

    private User testUser;
    private Mood testMood1;
    private Mood testMood2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .password("password123")
                .build();

        testUser = entityManager.persistAndFlush(testUser);

        testMood1 = Mood.builder()
                .emoji("😊")
                .score(8)
                .note("Test note 1")
                .entryDate(LocalDateTime.now().minusDays(1))
                .user(testUser)
                .build();

        testMood2 = Mood.builder()
                .emoji("😔")
                .score(5)
                .note("Test note 2")
                .entryDate(LocalDateTime.now())
                .user(testUser)
                .build();
    }

    @Test
    void findAllByUserIdOrderByEntryDateDesc_ReturnsMoodsInDescendingOrder() {
        entityManager.persist(testMood1);
        entityManager.persist(testMood2);
        entityManager.flush();

        List<Mood> moods = moodRepository.findAllByUserIdOrderByEntryDateDesc(testUser.getId());

        assertThat(moods).hasSize(2);
        assertThat(moods.get(0).getEmoji()).isEqualTo("😔");
        assertThat(moods.get(1).getEmoji()).isEqualTo("😊");
    }

    @Test
    void findAllByUserIdOrderByEntryDateDesc_WhenNoMoods_ReturnsEmptyList() {
        List<Mood> moods = moodRepository.findAllByUserIdOrderByEntryDateDesc(testUser.getId());

        assertThat(moods).isEmpty();
    }

    @Test
    void save_WhenMoodIsValid_SavesSuccessfully() {
        Mood savedMood = moodRepository.save(testMood1);

        assertThat(savedMood.getId()).isNotNull();
        assertThat(savedMood.getEmoji()).isEqualTo("😊");
        assertThat(savedMood.getCreatedAt()).isNotNull();
        assertThat(savedMood.getUser()).isEqualTo(testUser);
    }
}