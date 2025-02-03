/*package com.finpro.FinancePro.repository;

import com.finpro.FinancePro.entity.Provider;
import com.finpro.FinancePro.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByEmail_ExistingEmail() {
        User user = new User();
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setProvider(Provider.SELF);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getFullName());
    }

    @Test
    public void testFindByEmail_NonExistingEmail() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertTrue(foundUser.isEmpty());
    }

    @Test
    public void testSaveUser() {
        User newUser = new User();
        newUser.setFullName("Jane Smith");
        newUser.setEmail("jane@example.com");
        newUser.setPassword("anotherEncodedPassword");
        newUser.setProvider(Provider.SELF);

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("Jane Smith", savedUser.getFullName());
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setProvider(Provider.SELF);

        User savedUser = userRepository.save(user);
        userRepository.deleteById(savedUser.getId());

        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertTrue(deletedUser.isEmpty());
    }
}

 */