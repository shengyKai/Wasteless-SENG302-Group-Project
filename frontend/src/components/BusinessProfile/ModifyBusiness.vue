<template>
  <v-container>
    <v-form v-model="valid" ref="modifyForm">
      <v-card class="pb-2">
        <v-card-title class="primary-text">Modify Business Profile</v-card-title>
        <v-card-text>
          <!-- Business Modifier Tab -->
          <v-tabs v-model="tab">
            <v-tab key="info">Info</v-tab>
            <v-tab key="address">Address</v-tab>
            <v-tab key="image">Image</v-tab>
          </v-tabs>
          <!-- Have a v-model 'tab' that allows user to switch between different sections -->
          <v-tabs-items v-model="tab" class="pt-10" :eager="true">
            <!-- Business information tab -->
            <v-tab-item key="info" :eager="true">
              <v-row no-gutters>
                <v-col cols="12" sm="6">
                  <!-- INPUT: Business Name -->
                  <v-text-field
                    dense
                    class="mr-1 required"
                    v-model="businessName"
                    label="Name of business"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetExtendedSingleLineRules())"
                    outlined
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <!-- INPUT: Business Type -->
                  <v-select
                    dense
                    class="ml-1 required"
                    v-model="businessType"
                    :items="businessTypes"
                    label="Business Type"
                    :rules="mandatoryRules()"
                    outlined
                  />
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12" sm="12">
                  <!-- INPUT: Description -->
                  <v-textarea
                    dense
                    v-model="description"
                    label="Description"
                    :rules="maxCharDescriptionRules().concat(alphabetExtendedMultilineRules())"
                    rows="3"
                    outlined
                  />
                </v-col>
              </v-row>
              <!-- Sub section in info tab for admin modifying -->
              <div v-if="isPrimaryOwner || isSystemAdmin" class="mt-1">
                <v-card-title>Change Primary Administrator</v-card-title>
                <v-row>
                  <v-col>
                    <!-- INPUT: Admin -->
                    <span v-for="admin in administrators" :key="admin.id">
                      <v-chip
                        v-if="adminIsPrimary(admin)"
                        class="ma-1 ml-0"
                        color="red"
                        text-color="white"
                      >
                        {{ admin.firstName }} {{ admin.lastName }}
                      </v-chip>
                      <v-chip
                        v-else
                        class="ma-1 ml-0"
                        color="green"
                        text-color="white"
                        @click="changePrimaryOwner(admin)"
                      >
                        {{ admin.firstName }} {{ admin.lastName }}
                      </v-chip>
                    </span>
                  </v-col>
                </v-row>
                <!-- Admin modifying error message -->
                <v-row>
                  <v-alert v-if="showChangeAdminAlert" color="red" type="error" dense text>
                    {{ primaryAdminAlertMsg }}
                  </v-alert>
                </v-row>
              </div>
            </v-tab-item>
            <!-- Business address tab -->
            <v-tab-item key="address" :eager="true">
              <v-row no-gutters>
                <v-col cols="12" sm="6">
                  <!-- INPUT: Street Address -->
                  <v-text-field
                    class="mr-1 required"
                    v-model="streetAddress"
                    label="Company Street Address"
                    :rules="mandatoryRules().concat(streetRules())"
                    outlined/>
                </v-col>
                <v-col cols="12" sm="6">
                  <!-- INPUT: District -->
                  <LocationAutocomplete
                    type="district"
                    class="ml-1"
                    v-model="district"
                    :rules="maxCharRules().concat(alphabetRules())"
                  />
                </v-col>
              </v-row>
              <v-row no-gutters>
                <v-col cols="12" sm="6">
                  <!-- INPUT: City -->
                  <LocationAutocomplete
                    type="city"
                    class="mr-1 required"
                    v-model="city"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <!-- INPUT: Region -->
                  <LocationAutocomplete
                    type="region"
                    class="ml-1 required"
                    v-model="region"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
              </v-row>
              <v-row no-gutters>
                <v-col cols="12" sm="6">
                  <!-- INPUT: Country -->
                  <LocationAutocomplete
                    type="country"
                    class="mr-1 required"
                    v-model="country"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <!-- INPUT: Postcode -->
                  <v-text-field
                    class="ml-1 required"
                    v-model="postcode"
                    label="Postcode"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(postcodeRules())"
                    outlined
                  />
                </v-col>
              </v-row>
              <v-row justify="end" no-gutters>
                <!-- INPUT: Update Currency -->
                <v-checkbox
                  v-model="updateProductCountry"
                  class="mt-n5 mb-3"
                  label="Update catalogue's currency"
                  color="primary"
                  hide-details
                />
              </v-row>
            </v-tab-item>
            <!-- Business image tab -->
            <v-tab-item key="image" :eager="true">
              <ImageManager v-model="images"/>
              <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            </v-tab-item>
          </v-tabs-items>
          <v-divider/>
          <v-row>
            <!-- Showing the error message if caught -->
            <p class="error-text mt-1" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            <v-col class="text-right mt-3 mb-n3">
              <!-- INPUT: Submit -->
              <v-btn
                type="submit"
                ref="submitButton"
                color="primary"
                :disabled=!valid
                @click.prevent="openCurrencyDialog"
              >
                Submit
                <v-icon
                  class="ml-1 mr-1"
                  color="white"
                >
                  mdi-file-upload-outline
                </v-icon>
              </v-btn>
              <!-- INPUT: Discard -->
              <v-btn
                color="secondary"
                ref="discardButton"
                class="ml-2"
                @click="discardButton"
              > Discard
                <v-icon
                  color="white"
                >
                  mdi-file-cancel-outline
                </v-icon>
              </v-btn>
            </v-col>
          </v-row>
          <v-dialog
            ref="confirmDialog"
            v-model="currencyConfirmDialog"
            max-width="300px"
          >
            <!-- INPUT: Confirm Dialog -->
            <v-card>
              <v-card-title>
                Are you sure?
              </v-card-title>
              <v-card-text>
                Updating location for catalogue entries will change all of the listed product(s) currency accordingly
              </v-card-text>
              <v-card-actions>
                <v-spacer/>
                <v-btn
                  color="primary"
                  text
                  @click="proceedWithModifyBusiness()"
                >
                  Save Change
                </v-btn>
                <v-btn
                  color="primary"
                  text
                  @click="currencyConfirmDialog = false"
                >
                  Cancel
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </v-card-text>
      </v-card>
    </v-form>
  </v-container>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import ImageManager from "@/components/image/ImageManager";
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules,
  USER_ROLES
} from "@/utils";
import { modifyBusiness } from '@/api/business';
import { getUser } from '@/api/user';
export default {
  name: 'ModifyBusiness',
  components: {
    LocationAutocomplete,
    ImageManager
  },
  props: {
    business: Object
  },
  data() {
    return {
      tab: 'location',
      currencyConfirmDialog: false,
      readableAddress: "",
      errorMessage: undefined,
      dialog: true,
      administrators: this.business.administrators,
      businessName: this.business.name,
      description: this.business.description,
      businessType: this.business.businessType,
      streetAddress: this.business.address.streetNumber + " " + this.business.address.streetName,
      district: this.business.address.district,
      city: this.business.address.city,
      region: this.business.address.region,
      country: this.business.address.country,
      postcode: this.business.address.postcode,
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      updateProductCountry: false,
      valid: false,
      showAlert: false,
      showChangeAdminAlert: false,
      primaryAdminAlertMsg: "",
      primaryAdministratorId: this.business.primaryAdministratorId,
      maxCharRules: () => maxCharRules(100),
      maxCharDescriptionRules: ()=> maxCharRules(200),
      mandatoryRules: ()=> mandatoryRules,
      alphabetExtendedSingleLineRules: ()=> alphabetExtendedSingleLineRules,
      alphabetExtendedMultilineRules: ()=> alphabetExtendedMultilineRules,
      alphabetRules: ()=> alphabetRules,
      streetRules: ()=> streetNumRules,
      postcodeRules: ()=> postCodeRules,
      isLoading: false,
      images: this.business.images,
    };
  },
  computed: {
    /**
     * Check if the current user is the admin of the system
     */
    isSystemAdmin() {
      return [USER_ROLES.DGAA, USER_ROLES.GAA].includes(
        this.$store.getters.role
      );
    },
    /**
     * Check if the current user is the primary owner of the business
     */
    isPrimaryOwner() {
      return this.$store.state.user.id === this.business.primaryAdministratorId;
    },
    /**
     * Returns a list of IDs of the business's images
     */
    imageIds() {
      return this.images.map(image => image.id);
    }
  },
  methods: {
    /**
     * Opens the currency confirmation dialog if the updateProductCountry is checked
     */
    openCurrencyDialog() {
      if (this.updateProductCountry === false) {
        this.proceedWithModifyBusiness();
      } else {
        this.currencyConfirmDialog = true;
      }
    },
    /**
     * Loop through all admin to identify business primary owner
     * Return TRUE if the current looping chip is primary owner and no other chip is selected by user
     * If other chip is selected, the chip will be displayed as primary owner with message prompted
     */
    adminIsPrimary(admin) {
      return admin.id === this.primaryAdministratorId;
    },
    /**
     * Execute the discard modify functionality and render business profile
     */
    discardButton() {
      this.$emit('discardModifyBusiness');
    },
    /**
     * Action(s) of modifying a business
     * Get the street number and name from the street address field.
     * Check existence of new selected primary owner, update to new owner or remain unchange
     * Set up the modified fields
     * Calling API
     */
    async proceedWithModifyBusiness() {
      this.errorMessage = undefined;

      const streetParts = this.streetAddress.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");

      let modifiedFields = {
        primaryAdministratorId: this.primaryAdministratorId,
        name: this.businessName,
        description: this.description,
        address: {
          streetNumber: streetNum,
          streetName: streetName,
          district: this.district,
          city: this.city,
          region: this.region,
          country: this.country,
          postcode: this.postcode
        },
        businessType: this.businessType,
        updateProductCountry: this.updateProductCountry,
        imageIds: this.imageIds,
      };
      const result = await modifyBusiness(this.business.id, modifiedFields);

      /**
       * If the result is a string, means it is an error message, of which it will show up on the page.
       * As such, the modify page will still remain.
       * If the result is undefined(the else case) then an event will be emitted to the parent component, BusinessProfile/index.vue
       * and the modifyBusiness attribute there will be false, thus changing the page to the usual business profile page.
       */
      if (typeof result === 'string') {
        this.errorMessage = result;
      }

      // Updates the $store.state.user.businessesAdministered property if we are administering this business
      if (this.administrators.some(admin => admin.id === this.$store.state.user.id)) {
        let user = await getUser(this.$store.state.user.id);
        if (typeof user !== 'string') {
          let initialRole = this.$store.state.activeRole;
          this.$store.commit('setUser', user);
          this.$store.commit('setRole', initialRole); // Make sure we don't get set to the default role
        }
      }

      if (this.errorMessage === undefined) {
        this.$emit("modifySuccess");
      }
    },
    /**
     * When the user clicks on an administrator a popup message will appear stating that they
     * have changed that user to the primary administrator of the business. Additionally, the
     * colour of said user will change to red, whilst the existing primary administrator will
     * change to green. Futhermore, if the original primary owner is clicked this
     * message will not appear.
     */
    changePrimaryOwner(admin) {
      if (admin.id !== this.business.primaryAdministratorId) {
        this.primaryAdminAlertMsg = `Primary admin will be changed to ${admin.firstName} ${admin.lastName}`;
        this.showChangeAdminAlert = true;
      } else {
        this.showChangeAdminAlert = false;
        this.primaryAdminAlertMsg = "";
      }
      this.primaryAdministratorId = admin.id;
    },
  },
};
</script>

