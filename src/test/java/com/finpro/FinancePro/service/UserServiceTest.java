package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private CreateUserDTO createUserDTO;
    private UpdateUserDTO updateUserDTO;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser = userRepository.save(testUser);

        // Initialize CreateUserDTO
        createUserDTO = new CreateUserDTO();
        createUserDTO.setFullName("New User");
        createUserDTO.setEmail("new@example.com");
        createUserDTO.setPassword("password");

        // Initialize UpdateUserDTO
        updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setId(testUser.getId());
        updateUserDTO.setFullName("Updated User");
        updateUserDTO.setEmail("updated@example.com");
    }

    @Test
    void createUser_Success() {
        UserResponseDTO response = userService.createUser(createUserDTO);

        assertNotNull(response, "Response should not be null");
        assertEquals(createUserDTO.getFullName(), response.getFullName(), "Full name should match");
        assertEquals(createUserDTO.getEmail(), response.getEmail(), "Email should match");
    }

    @Test
    void updateUser_Success() {
        UserResponseDTO response = userService.updateUser(updateUserDTO);

        assertNotNull(response, "Response should not be null");
        assertEquals(updateUserDTO.getFullName(), response.getFullName(), "Full name should match");
        assertEquals(updateUserDTO.getEmail(), response.getEmail(), "Email should match");
    }

    @Test
    void updateUser_UserNotFound() {
        updateUserDTO.setId(999L); // Non-existent user ID

        assertThrows(ResourceNotFoundException.class, () ->
                        userService.updateUser(updateUserDTO),
                "Should throw ResourceNotFoundException for non-existent user"
        );
    }

    @Test
    void getUser_Success() {
        UserResponseDTO response = userService.getUser(testUser.getId());

        assertNotNull(response, "Response should not be null");
        assertEquals(testUser.getId(), response.getId(), "User ID should match");
        assertEquals(testUser.getFullName(), response.getFullName(), "Full name should match");
        assertEquals(testUser.getEmail(), response.getEmail(), "Email should match");
    }

    @Test
    void getUser_UserNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                        userService.getUser(999L),
                "Should throw ResourceNotFoundException for non-existent user"
        );
    }

    @Test
    void deleteUser_Success() {
        userService.deleteUser(testUser.getId());

        assertFalse(userRepository.existsById(testUser.getId()), "User should no longer exist");
    }

    @Test
    void deleteUser_UserNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                        userService.deleteUser(999L),
                "Should throw ResourceNotFoundException for non-existent user"
        );
    }
}
