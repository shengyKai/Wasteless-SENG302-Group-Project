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
                    @click=manufacturedBeforeSellBy()
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
                    @input=checkDatesFieldsAreValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Sell By. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="sellBy"
                    label="Sell By"
                    type="date"
                    @input=checkDatesFieldsAreValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Best Before. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="bestBefore"
                    label="Best Before"
                    type="date"
                    @input=checkDatesFieldsAreValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Expires. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    v-model="expires"
                    label="Expires"
                    type="date"
                    @input=checkDatesFieldsAreValid()
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
      today: '',
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
      sellBy: "",
      bestBefore: "",
      expires: "",
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
    checkDatesFieldsAreValid() {
      let today = new Date();
      //checks manufactured cannot be after today
      if (this.manufactured > today && this.manufactured !== "") {
        this.errorMessage = "The manufactured date is after today!";
      //checks the sell by, best before and expires dates are after today
      } else if (this.sellBy < today && this.sellBy !== "") {
        this.errorMessage = "The sell by date is before today!";
      } else if (this.bestBefore < today && this.bestBefore !== "") {
        this.errorMessage = "The best before date is before today!";
      } else if (this.expires < today && this.expires !== "") {
        this.errorMessage = "The expires date is before today!";
      //checks that manufactured < sell by < best before < expires
      } else if (this.manufactured > this.sellBy && this.manufactured !== "" && this.sellBy !== "") {
        this.errorMessage = "The manufactured date cannot be after the sell by date!";
      } else if (this.sellBy > this.bestBefore && this.sellBy !== "" && this.bestBefore !== "") {
        this.errorMessage = "The sell by date cannot be after the best before date!";
      } else if (this.bestBefore > this.expires && this.bestBefore !== "" && this.expires !== "") {
        this.errorMessage = "The best before date cannot be before the expires date!";
      } else {
        this.errorMessage = undefined;
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