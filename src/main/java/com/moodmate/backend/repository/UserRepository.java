package com.moodmate.backend.repository;

import com.moodmate.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Veri tabanı ile uygulama arasındaki katman

    Optional<User> findByEmail(String email);
}