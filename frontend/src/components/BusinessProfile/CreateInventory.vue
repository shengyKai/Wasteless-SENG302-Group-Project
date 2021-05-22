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
                    :rules="maxCharRules.concat(smallPriceRules)"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Total Price. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model="totalPrice"
                    label="Total Price"
                    prefix="$"
                    :rules="maxCharRules.concat(hugePriceRules)"
                    outlined/>
                </v-col>
                <!-- INPUT: Manufactured. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="manufactured"
                    label="Manufactured"
                    type="date"
                    @input=checkManufacterDateValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Sell By. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="sellBy"
                    label="Sell By"
                    type="date"
                    @input=checkSellByDateValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Best Before. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="bestBefore"
                    label="Best Before"
                    type="date"
                    @input=checkBestBeforeDateValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Expires. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    v-model="expires"
                    label="Expires"
                    type="date"
                    @input=checkExpiresDateVaild()
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
              label="submit"
              color="primary"
              :disabled="!valid || !datesValid"
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
//import moment from 'moment';

export default {
  name: 'CreateInventory',
  components: {
  },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      valid: false,
      today: new Date(),
      mockProductList: [
        'Nathan Apple',
        'Connor Orange',
        'Edward Banana',
      ],
      productCode : "",
      quantity : "",
      pricePerItem: "",
      totalPrice: "",
      manufactured: "",
      manufacturedValid: true,
      sellBy: "",
      sellByValid: true,
      bestBefore: "",
      bestBeforeValid: true,
      expires: "",
      expiresValid: true,
      datesValid: true,
      //expires: new Date().toISOString().slice(0,10), //Keep this so the next person know what to use if he/she wan

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
      smallPriceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers (4digit)
        field => /(^\d{1,4}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
      hugePriceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers (6digit)
        field => /(^\d{1,6}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
    };
  },
  methods: {

    closeDialog() {
      this.$emit('closeDialog');
    },
    async CreateInventory() { //to see the attribute in console for debugging or testing, remove after this page is done
      console.log(this.expires);
      return;
    },
    async checkAllDatesValid() {
      //checks the booleans for all the dates are valid
      if (this.manufacturedValid && this.sellByValid && this.bestBeforeValid && this.expiresValid) {
        this.datesValid = true;
      } else {
        this.datesValid = false;
      }
    },
    async checkManufacterDateValid() {
      //checks manufactured cannot be after today and is before sell by
      let sellByDate = new Date(this.manufactured);
      let manufacturedDate = new Date(this.manufactured);
      this.manufacturedValid = false;
      if (manufacturedDate > this.today) {
        this.errorMessage = "The manufactured date is after today!";
      } else if (manufacturedDate > sellByDate) {
        this.errorMessage = "The manufactured date cannot be after the sell by date!";
      } else {
        this.errorMessage = undefined;
        this.manufacturedValid = true;
      }
      await this.checkAllDatesValid();
    },
    async checkSellByDateValid() {
      //checks sell by date cannot be before today and is after manufactured and before best before
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      let manufacturedDate = new Date(this.manufactured);
      this.sellByValid = false;
      if (sellByDate < this.today) {
        this.errorMessage = "The sell by date is before today!";
      } else if (sellByDate < manufacturedDate) {
        this.errorMessage = "The sell by date cannot be before the manufactured date!";
      } else if (sellByDate > bestBeforeDate) {
        this.errorMessage = "The sell by date cannot be after the best before date!";
      } else {
        this.errorMessage = undefined;
        this.sellByValid = true;
      }
      await this.checkAllDatesValid();
    },
    async checkBestBeforeDateValid() {
      //checks best before date cannot be before today and is after sell by date
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      this.bestBeforeValid = false;
      if (bestBeforeDate < this.today) {
        this.errorMessage = "The best before date is before today!";
      } else if (bestBeforeDate < sellByDate) {
        this.errorMessage = "The best before date cannot be before the sell by date!";
      } else if (bestBeforeDate > expiresDate) {
        this.errorMessage = "The best before date cannot be after the expires date!";
      } else {
        this.errorMessage = undefined;
        this.bestBeforeValid = true;
      }
      await this.checkAllDatesValid();
    },
    async checkExpiresDateVaild() {
      //checks expires date cannot be before today and is after best before date
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      this.expiresValid = false;
      if (expiresDate < this.today) {
        this.errorMessage = "The expires date is before today!";
      } else if (expiresDate < bestBeforeDate) {
        this.errorMessage = "The expires date cannot be before the best before date!";
      } else {
        this.errorMessage = undefined;
        this.expiresValid = true;
      }
      await this.checkAllDatesValid();
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