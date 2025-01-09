package com.finpro.FinancePro.entity;

import lombok.Data;

public class TwoFactorAuth {
    private boolean isEnabled = false;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
