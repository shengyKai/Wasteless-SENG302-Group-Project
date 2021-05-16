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
            <h4 class="primary--text">Create Inventory Item</h4>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <!-- INPUT: Product code Currently used v-selector to reduce typo probability from user -->
                <v-col cols="6">
                  <v-select
                    class="required"
                    solo
                    value = "product Code"
                    v-model="productCode"
                    :items="mockProductList"
                    label="Product Code"
                    :rules="mandatoryRules"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Quantity. Only allows number.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    solo
                    v-model="quantity"
                    label="Quantity"
                    :rules="mandatoryRules.concat(numberRules)"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Price per item. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model="pricePerItem"
                    label="Price Per Item"
                    prefix="$"
                    :rules="maxCharRules.concat(priceRules)"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Total Price. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model="totalPrice"
                    label="Total Price"
                    prefix="$"
                    :rules="maxCharRules.concat(priceRules)"
                    outlined/>
                </v-col>
                <!-- INPUT: Manufactured. Only allows Alphabet and Number.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="manufactured"
                    label="Manufactured"
                    append-icon="mdi-map-marker"
                    :rules="maxCharRules.concat(alphabetNumRules)"
                    outlined/>
                </v-col>
                <!-- INPUT: Sell By. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="sellBy"
                    label="Sell By"
                    type="date"
                    outlined/>
                </v-col>
                <!-- INPUT: Best Before. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="bestBefore"
                    label="Best Before"
                    type="date"
                    :rules="maxCharRules"
                    outlined/>
                </v-col>
                <!-- INPUT: Expires. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    solo
                    v-model="expires"
                    label="Expires"
                    type="date"
                    :rules="mandatoryRules"
                    outlined/>
                </v-col>
              </v-row>
              <!-- Error Message if textfield.value !valid -->
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
              @click.prevent="CreateInventory">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>

export default {
  name: 'CreateInventory',
  components: {
  },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      valid: false,
      mockProductList: [
        'Nathan Apple',
        'Connor Orange',
        'Edward Banana',
      ],
      alphabetNumRules: [
        field => ( field.length === 0 || /^[a-zA-Z0-9 ]+$/i.test(field)) || 'Naming must only contain alphabet or number'
      ],
      maxCharRules: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ],
      numberRules: [
        field => /(^[0-9]*$)/.test(field) || 'Must contain numbers only'
      ],
      priceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers
        field => /(^\d{1,5}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
      productCodeRules: [
        // Product code rules was not being used atm as decided to use a v-selector for this field
        field => field.length <= 15 || 'Reached max character limit: 15',
        field => !/ /.test(field) || 'Must not contain a space',
        field => /^[-A-Z0-9]+$/.test(field) || 'Must be all uppercase letters, numbers and dashes.',
        field => !this.unavailableProductCodes.includes(field) || 'Product code is unavailable',
      ]
    };
  },
  methods: {

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