package com.moodmate.backend.repository;

import com.moodmate.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Yılmaz")
                .lastName("Naycı")
                .email("test@example.com")
                .password("password123")
                .build();
    }

    @Test
    void findByEmail_WhenUserExists_ReturnsUser() {
        entityManager.persistAndFlush(testUser);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("Yılmaz");
    }

    @Test
    void findByEmail_WhenUserDoesNotExist_ReturnsEmpty() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void save_WhenUserIsValid_SavesSuccessfully() {
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }
}