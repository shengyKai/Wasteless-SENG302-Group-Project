package org.seng302.Entities;

import javax.persistence.Entity;

@Entity
public class DefaultGlobalApplicationAdmin extends Account{
    /**
     * Autogenerate DGAA so there is always an admin account present
     * DGAA can do anything, even above normal admin accounts
     */
    public DefaultGlobalApplicationAdmin() {
        this.setEmail("wasteless@seng302.com");
        this.setAuthenticationCodeFromPassword("T3amThr33IsTh3B3st");
        this.setIsDGAA(true);

    }
}
