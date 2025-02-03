package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.entity.Provider;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User mockUser;

    @BeforeEach
    public void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("John Doe");
        mockUser.setEmail("john@example.com");
        mockUser.setProvider(Provider.SELF);

        SecurityUtils.setTestUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        // Add this line to mock existsById
        when(userRepository.existsById(1L)).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @AfterEach
    public void tearDown() {
        SecurityUtils.clearTestUserId();
    }

    @Test
    public void testCreateUser() {
        CreateUserDTO createDTO = new CreateUserDTO();
        createDTO.setFullName("John Doe");
        createDTO.setEmail("john@example.com");
        createDTO.setPassword("password123");

        UserResponseDTO responseDTO = userService.createUser(createDTO);

        assertNotNull(responseDTO);
        assertEquals("John Doe", responseDTO.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testUpdateUser() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setId(1L);
        updateDTO.setFullName("Updated Name");
        updateDTO.setEmail("updated@example.com");

        UserResponseDTO updatedUser = userService.updateUser(updateDTO);

        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getFullName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testGetUser() {
        UserResponseDTO userDTO = userService.getUser(1L);

        assertNotNull(userDTO);
        assertEquals("John Doe", userDTO.getFullName());
    }

    @Test
    public void testDeleteUser() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}