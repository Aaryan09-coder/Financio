package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*") // Be more specific in production
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO createDTO) {
        try {
            UserResponseDTO user = userService.createUser(createDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error creating user: ", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponse("Error creating user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO updateDTO) {
        try {
            updateDTO.setId(id);
            UserResponseDTO user = userService.updateUser(updateDTO);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error updating user: ", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponse("Error updating user: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            UserResponseDTO user = userService.getUser(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user: ", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponse("Error fetching user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting user: ", e);
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponse("Error deleting user: " + e.getMessage()));
        }
    }

    // Error response class
    public static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}