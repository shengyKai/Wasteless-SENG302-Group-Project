<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="660px"
    >
      <v-form v-model="valid">
        <v-card>
          <v-card-title>
            <h4 class="primary--text">Create Sale Item</h4>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <!-- INPUT: Quantity. Only numbers are allowed.-->
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
                <!-- INPUT: Price per item. Auto generated.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="pricePerItem"
                    label="Price Per Item"
                    prefix="$"
                    :rules="maxCharRules.concat(priceRules)"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Information.-->
                <v-col cols="12">
                  <v-textarea
                    v-model="info"
                    label="Info"
                    rows="3"
                    :rules="infoRules"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Closing date.-->
                <v-col cols="12">
                  <v-text-field
                    v-model="closes"
                    label="Closes"
                    type="date"
                    @input=checkClosesDateValid()
                    outlined
                  />
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
              :disabled="!valid || !closesValid"
              @click.prevent="CreateSaleItem">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>
import {createSaleItem} from '@/api/internal';
export default {
  name: 'CreateSaleItem',
  components: {},
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      valid: false,
      today: new Date(),
      inventoryItem: "",
      quantity: "",
      pricePerItem: "",
      info: "",
      closes: "",
      closesValid: true,
      maxDate: new Date("5000-01-01"),

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
        field => /(^\d{1,4}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],

      infoRules: [
        field => (field.length <= 200) || 'Reached max character limit: 200',
        field => /(^[ a-zA-Z0-9@//$%&!'//#,//.//(//)//:;_-]*$)/.test(field) || 'Bio must only contain letters, numbers, and valid special characters'
      ],
    };
  },
  methods: {
    /**
		 * Closes the dialog
		 */
    closeDialog() {
      this.$emit('closeDialog');
    },
    /**
		 * Called when the form is submitted
		 * Request backend to create a sale item listing
		 * Empty attributes are set to undefined
		 */
    async CreateSaleItem() {
      this.errorMessage = undefined;
      let quantity;
      try {
        quantity = parseInt(this.quantity);
      } catch ( error ) {
        this.errorMessage = 'Could not parse field \'Quantity\'';
        return;
      }
      const saleItem = {
        quantity: quantity,
        pricePerItem: this.pricePerItem,
        info: this.info ? this.info : undefined,
        closes: this.closes ? this.closes : undefined
      };
      const result = await createSaleItem(saleItem);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.closeDialog();
      }
    },
    /**
     * Checks the closing date is valid
     */
    async checkClosesDateValid() {
      let closesDate = new Date(this.closes);
      this.closesValid = false;
      if (closesDate < this.today) {
        this.errorMessage = "The closing date cannot be before today!";
      } else if (closesDate > this.maxDate) {
        this.errorMessage = "The closing date cannot be thousands of years into the future!";
      } else {
        this.errorMessage = undefined;
        this.closesValid = true;
      }
    }
  },
};
</script>

<style scoped>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
</style>