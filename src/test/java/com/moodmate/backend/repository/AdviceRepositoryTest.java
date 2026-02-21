package com.moodmate.backend.repository;

import com.moodmate.backend.entity.Advice;
import com.moodmate.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AdviceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AdviceRepository adviceRepository;

    private User testUser;
    private Advice testAdvice1;
    private Advice testAdvice2;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .password("password123")
                .build();

        testUser = entityManager.persistAndFlush(testUser);

        testAdvice1 = Advice.builder()
                .user(testUser)
                .advice("Old advice")
                .build();

        testAdvice2 = Advice.builder()
                .user(testUser)
                .advice("New advice")
                .build();
    }

    @Test
    void findTopByUserIdOrderByCreatedAtDesc_ReturnsLatestAdvice() throws InterruptedException {
        entityManager.persist(testAdvice1);
        entityManager.flush();

        Thread.sleep(100);

        entityManager.persist(testAdvice2);
        entityManager.flush();

        Optional<Advice> latestAdvice = adviceRepository.findTopByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(latestAdvice).isPresent();
        assertThat(latestAdvice.get().getAdvice()).isEqualTo("New advice");
    }

    @Test
    void findTopByUserIdOrderByCreatedAtDesc_WhenNoAdvice_ReturnsEmpty() {
        Optional<Advice> latestAdvice = adviceRepository.findTopByUserIdOrderByCreatedAtDesc(testUser.getId());

        assertThat(latestAdvice).isEmpty();
    }

    @Test
    void save_WhenAdviceIsValid_SavesSuccessfully() {
        Advice savedAdvice = adviceRepository.save(testAdvice1);

        assertThat(savedAdvice.getId()).isNotNull();
        assertThat(savedAdvice.getAdvice()).isEqualTo("Old advice");
        assertThat(savedAdvice.getCreatedAt()).isNotNull();
        assertThat(savedAdvice.getUser()).isEqualTo(testUser);
    }
}