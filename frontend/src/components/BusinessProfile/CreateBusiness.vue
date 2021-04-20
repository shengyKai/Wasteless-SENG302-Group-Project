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
            <span class="headline create-business">Create Business</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="business"
                    label="Name of business"
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-textarea
                    v-model="description"
                    label="Description"
                    :rules="maxCharDescriptionRules"
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
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="street1"
                    label="Company Street Address"
                    :rules="mandatoryRules"
                    outlined/>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="street2"
                    label="Apartment, Suite, Unit, Building, Floor"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="district"
                    v-model="district"
                    :rules="maxCharRules"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="city"
                    class="required"
                    v-model="city"
                    :rules="mandatoryRules.concat(maxCharRules)"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="state"
                    class="required"
                    v-model="state"
                    :rules="mandatoryRules.concat(maxCharRules)"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="country"
                    class="required"
                    v-model="country"
                    :rules="mandatoryRules.concat(maxCharRules)"
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="postcode"
                    label="Postcode"
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined
                  />
                </v-col>
              </v-row>
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
              @click="createBusiness">
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

export default {
  name: 'CreateBusiness',
  components: {
    LocationAutocomplete,
  },
  data() {
    return {
      dialog: true,
      business: '',
      description: '',
      businessType: [],
      street1: '',
      street2: '',
      district: '',
      city: '',
      state: '',
      country: '',
      postcode: '',
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organization',
        'Non-profit organization',
        'Retail Trade',
      ],
      valid: false,
      maxCharRules: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
      ],
      maxCharDescriptionRules: [
        field => (field.length <= 200) || 'Reached max character limit: 200'
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ]
    };
  },
  methods: {
    createBusiness() {
      this.$router.push('/business/1');
      this.closeDialog();
    },
    closeDialog() {
      this.$emit('closeDialog');
    }
  }
};
</script>

<style>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}

.create-business {
  color: var(--v-primary-base);
  font-weight: bolder;
}
</style>