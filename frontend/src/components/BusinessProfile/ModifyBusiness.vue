<template>
  <div>
    <v-row justify="center">
      <v-col cols="10">
        <v-card max-width=1800px>
          <v-form v-model="valid">
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
                    <v-col>
                      <v-btn
                        @click="changeUpdateCountries"
                      >
                        Update catalogue entries to new country
                      </v-btn>
                      <v-icon v-if="updateProductCountry" large> mdi-check </v-icon>
                      <v-icon v-if="!updateProductCountry" large> mdi-close </v-icon>
                    </v-col>
                  </v-row>
                  <div v-if="userIsPrimaryAdmin">
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
                  <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
                </v-container>
              </v-card-text>
              <v-card-actions>
                <v-row justify="end">
                  <v-col cols="2" class="ma-1 mr-7">
                    <v-btn
                      type="submit"
                      color="primary"
                      :disabled="!valid"
                    >
                      <v-icon
                        class="expand-icon"
                        color="white"
                      >
                        mdi-file-upload-outline
                      </v-icon>
                      Submit
                    </v-btn>
                  </v-col>
                  <v-col cols="4" class="ma-1 mr-n9">
                    <v-btn
                      class="white--text"
                      color="secondary"
                      @click="discardButton"
                    >
                      <v-icon
                        class="expand-icon"
                        color="white"
                      >
                        mdi-file-cancel-outline
                      </v-icon>
                      Discard
                    </v-btn>
                  </v-col>
                </v-row>
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
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules
} from "@/utils";
export default {
  name: 'ModifyBusiness',
  components: {
    LocationAutocomplete,
  },
  props: {
    business: Object
  },
  data() {
    return {
      readableAddress: "",
      errorMessage: undefined,
      dialog: true,
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
      updateProductCountry: true,
      valid: false,
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
    administrators() {
      return this.business?.administrators || [];
    },
    userIsPrimaryAdmin() {
      return this.$store.state.user.id === this.business.primaryAdministratorId;
    }
  },
  methods: {
    adminIsPrimary(admin) {
      return admin.id === this.primaryAdministratorId;
    },
    changeUpdateCountries() {
      if (this.updateProductCountry) {
        this.updateProductCountry = false;
      } else {
        this.updateProductCountry = true;
      }
    },
    discardButton() {
      this.$emit('discardModifyBusiness');
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