package com.example.EliteCart.Repository;

import com.example.EliteCart.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    List<User> findByActive(boolean active);
     Optional<User> findByEmail(String email);
}
