<template>
  <div  class="d-flex flex-column" no-gutters>
    <v-row justify="center">
      <v-col cols="10">
        <v-card max-width=1800px>
          <v-form v-model="valid"  @submit.prevent="proceedWithModifyBusiness">
            <v-card class="mt-5 ">
              <v-card-title class="primary-text">Modify Business Details</v-card-title>
              <v-card-text>
                <v-container>
                  <v-row no-gutters>
                    <v-col cols="6">
                      <v-text-field
                        dense
                        class="mr-1 required"
                        v-model="businessName"
                        label="Name of business"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetExtendedSingleLineRules())"
                        outlined
                      />
                    </v-col>
                    <v-col cols="6">
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
                    <v-col cols="12">
                      <v-textarea
                        dense
                        v-model="description"
                        label="Description"
                        :rules="maxCharDescriptionRules().concat(alphabetExtendedMultilineRules())"
                        rows="3"
                        outlined
                      />
                    </v-col>
                    <v-col cols="12">
                      <v-card-title class="primary-text">Address</v-card-title>
                    </v-col>
                    <v-col cols="16">
                      <v-text-field
                        class="mr-1 required"
                        v-model="streetAddress"
                        label="Company Street Address"
                        :rules="mandatoryRules().concat(streetRules())"
                        outlined/>
                    </v-col>
                    <v-col cols="6">
                      <LocationAutocomplete
                        type="district"
                        class="ml-1"
                        v-model="district"
                        :rules="maxCharRules().concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="6">
                      <LocationAutocomplete
                        type="city"
                        class="mr-1 required"
                        v-model="city"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="6">
                      <LocationAutocomplete
                        type="region"
                        class="ml-1 required"
                        v-model="region"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="6">
                      <LocationAutocomplete
                        type="country"
                        class="mr-1 required"
                        v-model="country"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="6">
                      <v-text-field
                        class="ml-1 required"
                        v-model="postcode"
                        label="Postcode"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(postcodeRules())"
                        outlined
                      />
                    </v-col>
                    <v-col
                      cols="12"
                      sm="4"
                      md="4"
                    >
                      <v-checkbox
                        v-model="updateProductCountry"
                        label="Update catalogue's currency"
                        color="primary"
                        hide-details
                        @click.stop="currencyConfirmDialog = true"
                      />
                    </v-col>
                  </v-row>
                  <div v-if="userIsPrimaryAdmin" class="mt-5">
                    <v-card-title>Change Primary Administrator</v-card-title>
                    <v-col>
                      <v-row>
                        <span v-for="admin in administrators" :key="admin.id">
                          <v-chip
                            v-if="adminIsPrimary(admin)"
                            class="admin-chip"
                            color="red"
                            text-color="white"
                          >
                            {{ admin.firstName }} {{ admin.lastName }}
                          </v-chip>
                          <v-chip
                            v-else
                            class="admin-chip"
                            color="green"
                            text-color="white"
                            @click="changePrimaryAdmin(admin)"
                          >
                            {{ admin.firstName }} {{ admin.lastName }}
                          </v-chip>
                        </span>
                        <v-alert v-if="showChangeAdminAlert" color="red" type="error" dense text>
                          {{ primaryAdminAlertMsg }}
                        </v-alert>
                      </v-row>
                    </v-col>
                  </div>
                  <v-card-title>Images</v-card-title>
                  <v-btn
                    color="primary"
                    outlined
                    @click="showImageUploaderForm=true"
                  >
                    <v-icon
                      class="expand-icon"
                      color="primary"
                    >
                      mdi-upload
                    </v-icon>
                    Upload new image
                  </v-btn>
                  <BusinessImageUploader :business-id="business.id" v-model="showImageUploaderForm"/>
                  <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
                </v-container>
              </v-card-text>
              <v-card-actions>
                <v-row>
                  <v-col class="text-right">
                    <v-btn
                      type="submit"
                      color="primary"
                      :disabled="!valid"
                    >
                      Submit
                      <v-icon
                        class="ml-1 mr-1"
                        color="white"
                      >
                        mdi-file-upload-outline
                      </v-icon>
                    </v-btn>
                    <v-btn
                      color="secondary"
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
                        @click="currencyConfirmDialog = false; updateProductCountry = true;"
                      >
                        Save Change
                      </v-btn>
                      <v-btn
                        color="primary"
                        text
                        @click="currencyConfirmDialog = false; updateProductCountry = false;"
                      >
                        Cancel
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-dialog>
              </v-card-actions>
            </v-card>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import BusinessImageUploader from "@/components/utils/BusinessImageUploader";
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules
} from "@/utils";
import { modifyBusiness } from '@/api/internal';

export default {
  name: 'ModifyBusiness',
  components: {
    LocationAutocomplete,
    BusinessImageUploader,
  },
  props: {
    business: Object
  },
  data() {
    return {
      currencyConfirmDialog: false,
      serverUrl: process.env.VUE_APP_SERVER_ADD,
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
      images: this.business.images || [],
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      updateProductCountry: false,
      valid: false,
      showImageUploaderForm: false,
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
    };
  },
  computed: {
    userIsPrimaryAdmin() {
      return this.$store.state.user.id === this.business.primaryAdministratorId;
    }
  },
  methods: {
    adminIsPrimary(admin) {
      return admin.id === this.primaryAdministratorId;
    },
    discardButton() {
      this.$emit('discardModifyBusiness');
    },
    async proceedWithModifyBusiness() {
      this.errorMessage = undefined;
      /**
       * Get the street number and name from the street address field.
       */
      const streetParts = this.streetAddress.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");

      // Set up the modified fields
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
        updateProductCountry: this.updateProductCountry
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
      } else {
        this.$emit("modifySuccess");
      }
    },
    /**
     * When the user clicks on an administrator a popup message will appear stating that they
     * have changed that user to the primary administrator of the business. Additionally, the
     * colour of said user will change to red, whilst the existing primary administrator will
     * change to green. Futhermore, if the original primary administrator is clicked this
     * message will not appear.
     */
    changePrimaryAdmin(admin) {
      this.showChangeAdminAlert = true;
      if (admin.id !== this.business.primaryAdministratorId) {
        this.showChangeAdminAlert = true;
        this.primaryAdminAlertMsg = `Primary admin will be changed to ${admin.firstName} ${admin.lastName}`;
      } else {
        this.showChangeAdminAlert = false;
        this.primaryAdminAlertMsg = "";
      }
      this.primaryAdministratorId = admin.id;
    },
  }
};
</script>

<style scoped>
.body {
    padding: 16px;
    width: 100%;
    margin-top: 140px;
}

.top-section {
  display: flex;
  flex-wrap: wrap;
}

.admin-chip {
  margin-right: 4px;
}

.business-modify {
  margin-top: 20px;
}

.modify-business-button {
  display: block;
  margin-right: 48%;
}

.expand-icon {
  padding-right: 10px;
}
</style>