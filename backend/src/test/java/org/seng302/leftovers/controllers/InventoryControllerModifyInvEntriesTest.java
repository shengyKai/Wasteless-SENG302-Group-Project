package org.seng302.leftovers.controllers;

import org.junit.jupiter.api.Test;

public class InventoryControllerModifyInvEntriesTest {

    @Test
    void modifyInvEntries_notLoggedIn_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_invalidAuthToken_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyId_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyIdInvalid_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyName_modifiedEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyNameInvalid_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyDescription_modifiedEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyDescriptionInvalid_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyManufacturer_modifiedEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyManufacturerInvalid_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyRRP_modifiedEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyRRPInvalid_cannotModify400() {
        //TODO
    }

    @Test
    void modifyInvEntries_modifyAllFields_modifiedEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_isDGAA_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_isGAA_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsBusinessOwner_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsDifferentBusinessOwner_cannotModify403() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsBusinessAdmin_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_invEntryIsDifferentBusinessAdmin_cannotModify403() {
        //TODO
    }

    @Test
    void modifyInvEntries_validBusinessIdAndProductId_modifiedInvEntry200() {
        //TODO
    }

    @Test
    void modifyInvEntries_nonexistentBusinessId_invalidId400() {
        //TODO
    }

    @Test
    void modifyInvEntries_nonexistentProductId_invalidId40() {
        //TODO
    }

    @Test
    void modifyInvEntries_() {
        //TODO
    }
}
