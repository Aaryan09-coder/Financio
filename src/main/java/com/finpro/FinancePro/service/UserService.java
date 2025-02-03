package com.finpro.FinancePro.service;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.entity.User;
import com.finpro.FinancePro.exception.ResourceNotFoundException;
import com.finpro.FinancePro.repository.UserRepository;
import com.finpro.FinancePro.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(CreateUserDTO createDTO) {
        User user = new User();
        user.setFullName(createDTO.getFullName());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setEmail(createDTO.getEmail());
        user.setProvider(createDTO.getProvider());

        User savedUser = userRepository.save(user);
        return convertToResponseDTO(savedUser);
    }

    public UserResponseDTO updateUser(UpdateUserDTO updateDTO) {

        if (!SecurityUtils.isCurrentUserOrAdmin(updateDTO.getId())) {
            throw new AccessDeniedException("You are not authorized to update this user's data");
        }

        User user = userRepository.findById(updateDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + updateDTO.getId()));

        if (updateDTO.getFullName() != null) {
            user.setFullName(updateDTO.getFullName());
        }
        if (updateDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getProvider() != null) {
            user.setProvider(updateDTO.getProvider());
        }

        User updatedUser = userRepository.save(user);
        return convertToResponseDTO(updatedUser);
    }

    public UserResponseDTO getUser(Long id) {
        if (!SecurityUtils.isCurrentUserOrAdmin(id)) {
            throw new AccessDeniedException("You are not authorized to access this user's data");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToResponseDTO(user);
    }

    public void deleteUser(Long id) {
        if (!SecurityUtils.isCurrentUserOrAdmin(id)) {
            throw new AccessDeniedException("You are not authorized to delete this user");
        }

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setProvider(user.getProvider());
        return dto;
    }
}
