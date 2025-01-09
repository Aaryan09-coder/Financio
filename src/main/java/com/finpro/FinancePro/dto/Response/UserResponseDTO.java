package com.finpro.FinancePro.dto.Response;

import com.finpro.FinancePro.entity.Provider;

public class UserResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private Provider provider;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}
