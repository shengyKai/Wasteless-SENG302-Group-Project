<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <v-form v-model="valid">
        <v-card>
          <v-card-title>
            <h4 class="primary--text">Create Business</h4>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="business"
                    label="Name of business"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetExtendedSingleLineRules())"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-textarea
                    v-model="description"
                    label="Description"
                    :rules="maxCharDescriptionRules().concat(alphabetExtendedMultilineRules())"
                    rows="3"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-select
                    class="required"
                    v-model="businessType"
                    :items="businessTypes"
                    label="Business Type"
                    :rules="mandatoryRules()"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="streetAddress"
                    label="Company Street Address"
                    :rules="mandatoryRules().concat(streetRules())"
                    outlined/>
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="district"
                    v-model="district"
                    :rules="maxCharRules().concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="city"
                    class="required"
                    v-model="city"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="region"
                    class="required"
                    v-model="region"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="country"
                    class="required"
                    v-model="country"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="postcode"
                    label="Postcode"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(postcodeRules())"
                    outlined
                  />
                </v-col>
              </v-row>
              <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer/>
            <v-btn
              color="primary"
              text
              @click="closeDialog">
              Close
            </v-btn>
            <v-btn
              type="submit"
              color="primary"
              :disabled="!valid"
              @click.prevent="createBusiness">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules
} from "@/utils";
import {createBusiness} from "@/api/business";

export default {
  name: 'CreateBusiness',
  components: {
    LocationAutocomplete,
  },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      business: '',
      description: '',
      businessType: [],
      streetAddress: '',
      district: '',
      city: '',
      region: '',
      country: '',
      postcode: '',
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      valid: false,
      maxCharRules: () => maxCharRules(100),
      maxCharDescriptionRules: ()=> maxCharRules(200),
      mandatoryRules: ()=> mandatoryRules,
      alphabetExtendedSingleLineRules: ()=> alphabetExtendedSingleLineRules,
      alphabetExtendedMultilineRules: ()=> alphabetExtendedMultilineRules,
      alphabetRules: ()=> alphabetRules,
      streetRules: ()=> streetNumRules,
      postcodeRules: ()=> postCodeRules
    };
  },
  methods: {
    async createBusiness() {
      this.errorMessage = undefined;
      /**
       * Get the street number and name from the street address field.
       */
      const streetParts = this.streetAddress.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");
      let business = {
        primaryAdministratorId: this.$store.state.user.id,
        name: this.business,
        description: this.description,
        address: {
          streetNumber: streetNum,
          streetName: streetName,
          district: this.district,
          city: this.city,
          region: this.region,
          country: this.country,
          postcode: this.postcode,
        },
        businessType: this.businessType,
      };
      let response = await createBusiness(business);
      if (response === undefined) {
        this.closeDialog();
        this.$router.go();
      } else {
        this.errorMessage = response;
      }
    },
    closeDialog() {
      this.$emit('closeDialog');
    }
  }
};
</script>

<style scoped>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
</style>