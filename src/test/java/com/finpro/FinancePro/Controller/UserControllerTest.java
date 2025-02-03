package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        // Create a mock UserService (simplified for this example)
        userService = new UserService() {
            @Override
            public UserResponseDTO createUser(CreateUserDTO createDTO) {
                if (createDTO.getFullName() == null || createDTO.getFullName().isEmpty()) {
                    throw new IllegalArgumentException("Invalid user data");
                }
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setFullName(createDTO.getFullName());
                userResponseDTO.setEmail(createDTO.getEmail());
                return userResponseDTO;
            }

            @Override
            public UserResponseDTO updateUser(UpdateUserDTO updateDTO) {
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setId(updateDTO.getId());
                userResponseDTO.setFullName(updateDTO.getFullName());
                return userResponseDTO;
            }

            @Override
            public UserResponseDTO getUser(Long id) {
                UserResponseDTO userResponseDTO = new UserResponseDTO();
                userResponseDTO.setId(id);
                userResponseDTO.setFullName("Test User");
                return userResponseDTO;
            }

            @Override
            public void deleteUser(Long id) {
                // Simulate delete operation
            }
        };

        // Use reflection to set userService in the controller
        try {
            Field serviceField = UserController.class.getDeclaredField("userService");
            serviceField.setAccessible(true);
            serviceField.set(userController, userService);
        } catch (Exception e) {
            throw new RuntimeException("Could not set userService", e);
        }
    }

    @Test
    void testCreateUser_Success() {
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.setFullName("John Doe");
        createDTO.setEmail("john@example.com");
        createDTO.setPassword("password123");

        ResponseEntity<?> response = userController.createUser(createDTO);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof UserResponseDTO);

        UserResponseDTO userResponse = (UserResponseDTO) response.getBody();
        assertEquals("John Doe", userResponse.getFullName());
        assertEquals("john@example.com", userResponse.getEmail());
    }

    @Test
    void testCreateUser_Exception() {
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.setFullName("");
        createDTO.setEmail("");
        createDTO.setPassword("");

        ResponseEntity<?> response = userController.createUser(createDTO);

        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());

        // Add more robust error checking
        Object body = response.getBody();
        assertTrue(body instanceof UserController.ErrorResponse, "Body should be ErrorResponse");

        UserController.ErrorResponse errorResponse = (UserController.ErrorResponse) body;
        assertNotNull(errorResponse.getMessage(), "Error message should not be null");
        assertTrue(errorResponse.getMessage().contains("Error creating user"), "Error message should contain expected text");
    }
    @Test
    void testUpdateUser_Success() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setId(1L);
        updateDTO.setFullName("Updated Name");

        ResponseEntity<?> response = userController.updateUser(1L, updateDTO);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof UserResponseDTO);

        UserResponseDTO userResponse = (UserResponseDTO) response.getBody();
        assertEquals(1L, userResponse.getId());
        assertEquals("Updated Name", userResponse.getFullName());
    }

    @Test
    void testGetUser_Success() {
        ResponseEntity<?> response = userController.getUser(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof UserResponseDTO);

        UserResponseDTO userResponse = (UserResponseDTO) response.getBody();
        assertEquals(1L, userResponse.getId());
        assertEquals("Test User", userResponse.getFullName());
    }

    @Test
    void testDeleteUser_Success() {
        ResponseEntity<?> response = userController.deleteUser(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
    }
}