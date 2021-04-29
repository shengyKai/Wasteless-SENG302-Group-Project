<template>
  <v-row justify="center">
    <v-dialog
      v-model="dialog"
      persistent
      max-width="600px"
    >
      <v-form
        v-model="valid"
      >
        <v-card>
          <v-card-title>
            <span class="headline create-product">Create new Product</span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="product"
                    label="Name of product"
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
                <v-col cols="6">
                  <v-text-field
                    v-model="manufacturer"
                    label="Manufacturer"
                    :rules="maxCharRules"
                    outlined
                  />
                </v-col>
                <v-col cols="6">
                  <v-menu
                    v-model="dateMenu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    nudge-right="40"
                    offset-y
                    min-width="auto"
                  >
                    <template v-slot:activator="{ on, attrs }">
                      <v-text-field
                        v-model="expiryDate"
                        label="Expiry Date"
                        prepend-icon="mdi-calendar"
                        readonly
                        v-bind="attrs"
                        v-on="on"
                        outlined
                      />
                    </template>
                    <v-date-picker
                      v-model="expiryDate"
                      :allowed-dates="allowedDates"
                      show-current
                      no-title
                      scrollable
                      @input="dateMenu=false"
                    />
                  </v-menu>
                </v-col>
                <v-col cols="7">
                  <v-text-field
                    v-model="recommendedRetailPrice"
                    label="Recommended Retail Price"
                    :prefix="currency.symbol"
                    :suffix="currency.code"
                    :rules="priceRules"
                    outlined
                  />
                </v-col>
                <v-col cols="5">
                  <v-text-field
                    v-model="quantity"
                    label="Quantity"
                    type="number"
                    :rules="quantityRules"
                    single-line
                    outlined
                  />
                </v-col>
                <v-col>
                  <v-text-field
                    v-model="productCode"
                    label="Short-hand Product Code"
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
              @click.prevent="createProduct">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>
import {createProduct, getBusiness} from '../../api';
import {currencyFromCountry} from "@/components/utils/Methods/currency";
export default {
  name:'CreateProduct',
  data() {
    return {
      dialog: true,
      dateMenu: false,
      product: '',
      description: '',
      expiryDate: '',
      manufacturer: '',
      recommendedRetailPrice: '',
      quantity: '',
      productCode: '',
      currency: {},
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
      ],
      priceRules: [
        //A price must be numbers and may contain a decimal followed by exactly two numbers
        field => /(^\d{1,5}(\.\d{2})?$)|^$/.test(field) || 'Must be a valid price'
      ],
      quantityRules: [
        field => field >= 0 || 'Must be a positive number',
        field => field <= 100000 || 'Must not be too large'
      ]
    };
  },
  async created() {
    // When the create product dialogue, the currency will be set to the currency of the country the product is being
    // sold in. It will default to New Zealand Dollars if no currency can be found from the country.
    if (this.$store.state.activeRole.type !== 'business') {
      console.warn("Active role is not business");
      return;
    }
    const business = await getBusiness(this.$store.state.activeRole.id);
    const countryOfSale = business.address.country;
    this.currency = await currencyFromCountry(countryOfSale);
  },
  methods: {
    /**
     * Creates the product by calling the API
     **/
    async createProduct() {
      //TODO merge with backend
      await createProduct({
        name: this.$data.product,
        description: this.$data.description,
        manufacturer: this.$data.manufacturer,
        expiryDate: this.$data.expiryDate,
        recommendedRetailPrice: this.$data.recommendedRetailPrice,
        quantity: this.$data.quantity,
        productCode: this.$data.productCode
      });
      this.closeDialog();
    },
    closeDialog() {
      this.$emit('closeDialog');
    },
    /**
     * Accepts only dates in the future
     * @param val
     * @returns {boolean}
     */
    allowedDates: val => new Date(val) > new Date(),
  },
};
</script>

<style>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}

.create-product {
  color: #558b2f; /* TODO Set this to primary colour variable */
  font-weight: bolder;
}
</style>