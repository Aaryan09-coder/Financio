package com.finpro.FinancePro.dto.Request;

import com.finpro.FinancePro.entity.Provider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateUserDTO {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Password is required")
    private String password;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private Provider provider = Provider.SELF;

    public @NotBlank(message = "Full name is required") String getFullName() {
        return fullName;
    }

    public void setFullName(@NotBlank(message = "Full name is required") String fullName) {
        this.fullName = fullName;
    }

    public @NotBlank(message = "Password is required") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") String password) {
        this.password = password;
    }

    public @Email(message = "Invalid email format") @NotBlank(message = "Email is required") String getEmail() {
        return email;
    }

    public void setEmail(@Email(message = "Invalid email format") @NotBlank(message = "Email is required") String email) {
        this.email = email;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}
