package com.finpro.FinancePro.Controller;

import com.finpro.FinancePro.dto.Request.CreateUserDTO;
import com.finpro.FinancePro.dto.Request.UpdateUserDTO;
import com.finpro.FinancePro.dto.Response.UserResponseDTO;
import com.finpro.FinancePro.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserDTO createDTO) {
        UserResponseDTO user = userService.createUser(createDTO);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO updateDTO) {
        updateDTO.setId(id);
        UserResponseDTO user = userService.updateUser(updateDTO);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        UserResponseDTO user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
