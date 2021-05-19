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
                    no-data-text="No products found"
                    class="required"
                    v-model="productCode"
                    :items="filteredProductList"
                    item-text="name"
                    item-value="id"
                    label="Product"
                    :rules="mandatoryRules"
                    :hint="productCode"
                    @click="productCode=undefined"
                    persistent-hint
                    outlined
                  >
                    <template v-slot:prepend-item>
                      <v-list-item>
                        <v-list-item-content>
                          <v-text-field
                            label="Search for a product"
                            v-model="productFilter"
                            clearable
                            :autofocus="true"
                            @click:clear="resetSearch"
                            v-on:keyup="filterProducts"
                            hint="Id, Name, Description, Manufacturer"
                          />
                        </v-list-item-content>
                      </v-list-item>
                    </template>
                  </v-select>
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
                    :max="today"
                    outlined/>
                </v-col>
                <!-- INPUT: Sell By. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="sellBy"
                    label="Sell By"
                    type="date"
                    :min="today"
                    outlined/>
                </v-col>
                <!-- INPUT: Best Before. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="bestBefore"
                    label="Best Before"
                    type="date"
                    :min="today"
                    outlined/>
                </v-col>
                <!-- INPUT: Expires. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    v-model="expires"
                    label="Expires"
                    type="date"
                    :min="today"
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

import {getProducts} from "@/api/internal";

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
      productList: [],
      filteredProductList: [],
      productFilter: '',
      productCode : "",
      quantity : "",
      pricePerItem: "",
      totalPrice: "",
      manufactured: "",
      sellBy: "",
      bestBefore: "",
      expires: new Date().toISOString().slice(0,10), //Keep this so the next person know what to use if he/she wan

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
    /**
     * Populates the products array for the dropdown select for selecting a product
     * @returns {Promise<void>}
     */
    async fetchProducts() {
      // get the list of products for this business
      const result = await getProducts(this.$store.state.createInventoryDialog, null, null, null, false);
      if (typeof result === 'string') {
        this.errorMessage = result;
        console.log(result);
        return;
      } else {
        this.productList = result;
        this.filteredProductList = result;
      }
    },
    /**
     * Filters the list of products based on the value of the search term
     * value must be passed to ensure products are refreshed when input is cleared
     */
    filterProducts: function() {
      if (this.productFilter === null) this.productFilter = '';
      this.filteredProductList = this.productList.filter(x => this.filterPredicate(x));
    },
    resetSearch: function() {
      this.productFilter = '';
      this.filterProducts();
    },
    /**
     * Defines a predicate used for filtering the available products
     * Predicate matches Id, Name, Manufacturer and Description
     * @param product The product to compare
     * @returns {boolean|undefined}
     */
    filterPredicate(product) {
      return product.id.toLowerCase().includes(this.productFilter?.toLowerCase()) ||
          product.name.toLowerCase().includes(this.productFilter?.toLowerCase()) ||
          product.manufacturer?.toLowerCase().includes(this.productFilter?.toLowerCase()) ||
          product.description?.toLowerCase().includes(this.productFilter?.toLowerCase());
    }
  },
  async created() {
    await this.fetchProducts();
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