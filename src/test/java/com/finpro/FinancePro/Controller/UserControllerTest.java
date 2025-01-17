package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.entity.Provider;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserControllerTest {

    @Autowired
    private UserController userController;

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
        testUser.setPassword("password123");
        testUser.setProvider(Provider.SELF);
        testUser = userRepository.save(testUser);

        // Initialize CreateUserDTO
        createUserDTO = new CreateUserDTO();
        createUserDTO.setFullName("New User");
        createUserDTO.setEmail("new@example.com");
        createUserDTO.setPassword("newpassword123");
        createUserDTO.setProvider(Provider.SELF);

        // Initialize UpdateUserDTO
        updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setId(testUser.getId());
        updateUserDTO.setFullName("Updated User");
        updateUserDTO.setEmail("updated@example.com");
        updateUserDTO.setProvider(Provider.SELF);
    }

    @Test
    void createUser_Success() {
        // Act
        ResponseEntity<UserResponseDTO> response = userController.createUser(createUserDTO);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        UserResponseDTO userResponse = response.getBody();
        assertNotNull(userResponse, "Response body should not be null");
        assertEquals(createUserDTO.getFullName(), userResponse.getFullName(), "Full name should match");
        assertEquals(createUserDTO.getEmail(), userResponse.getEmail(), "Email should match");
        assertEquals(createUserDTO.getProvider(), userResponse.getProvider(), "Provider should match");
    }

    @Test
    void updateUser_Success() {
        // Act
        ResponseEntity<UserResponseDTO> response = userController.updateUser(testUser.getId(), updateUserDTO);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        UserResponseDTO userResponse = response.getBody();
        assertNotNull(userResponse, "Response body should not be null");
        assertEquals(updateUserDTO.getFullName(), userResponse.getFullName(), "Full name should match");
        assertEquals(updateUserDTO.getEmail(), userResponse.getEmail(), "Email should match");
        assertEquals(testUser.getId(), userResponse.getId(), "User ID should match");
    }

    @Test
    void getUser_Success() {
        // Act
        ResponseEntity<UserResponseDTO> response = userController.getUser(testUser.getId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");

        UserResponseDTO userResponse = response.getBody();
        assertNotNull(userResponse, "Response body should not be null");
        assertEquals(testUser.getId(), userResponse.getId(), "User ID should match");
        assertEquals(testUser.getFullName(), userResponse.getFullName(), "Full name should match");
        assertEquals(testUser.getEmail(), userResponse.getEmail(), "Email should match");
    }

    @Test
    void getUser_UserNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                        userController.getUser(999L),
                "Should throw ResourceNotFoundException for non-existent user"
        );
    }

    @Test
    void deleteUser_Success() {
        // Act
        ResponseEntity<Void> response = userController.deleteUser(testUser.getId());

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals(200, response.getStatusCodeValue(), "Status code should be 200");
        assertFalse(userRepository.existsById(testUser.getId()), "User should no longer exist");
    }

    @Test
    void deleteUser_UserNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                        userController.deleteUser(999L),
                "Should throw ResourceNotFoundException for non-existent user"
        );
    }
}