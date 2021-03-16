<template>
  <v-form v-model="valid">
    <v-row justify="center">
      <v-dialog v-model="dialog" persistent max-width="600px">
        <template v-slot:activator="{ on, attrs }">
          <v-btn color="primary" dark v-bind="attrs" v-on="on">
            Create Now
          </v-btn>
        </template>
        <v-card>
          <v-card-title>
            <span class="headline">Create Business</span>
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
                    chips
                    multiple
                    outlined
                  />
                </v-col>
                <v-col cols="12" sm="6" md="12">
                  <v-text-field
                    class="required"
                    v-model="street1"
                    label="Company Street Address"
                    :rules="mandatoryRules"
                    outlined />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="street2"
                    label="Apartment, Suite, Unit, Building, Floor"
                    outlined
                  />
                </v-col>
                <v-col cols="12" sm="6" md="6">
                  <DistrictAutocomplete
                    v-model="district"/>
                </v-col>
                <v-col cols="12" sm="6" md="6">
                  <CityAutocomplete
                    class="required"
                    v-model="city"
                  />
                </v-col>
                <v-col cols="12" sm="6" md="6">
                  <StateAutocomplete
                    class="required"
                    v-model="state"
                  />
                </v-col>
                <v-col cols="12" sm="6" md="6">
                  <CountryAutocomplete
                    class="required"
                    v-model="country"
                  />
                </v-col>
                <v-col cols="12" sm="6" md="6">
                  <v-text-field
                    class="required"
                    v-model="postcode"
                    label="Postcode"
                    :rules="mandatoryRules.concat(maxCharRules)"
                    outlined />
                </v-col>
              </v-row>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn color="primary" text @click="dialog = false">
              Close
            </v-btn>
            <v-btn
              type="submit"
              color="primary"
              :disabled="!valid"
              @click="dialog = false">
              Save
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-row>
  </v-form>
</template>

<script>
import CountryAutocomplete from '@/components/utils/CountryAutocomplete';
import DistrictAutocomplete from '@/components/utils/DistrictAutocomplete';
import CityAutocomplete from '@/components/utils/CityAutocomplete';
import StateAutocomplete from '@/components/utils/StateAutocomplete';

export default {
  name: 'CreateBusiness',
  components: {
    CountryAutocomplete,
    DistrictAutocomplete,
    CityAutocomplete,
    StateAutocomplete
  },
  data () {
    return {
      dialog: false,
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
        'Retail Trade',
        'Charitable organization',
        'Non-profit organization',
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
  }
};
</script>

<style>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
</style>