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
            <span class="headline create-inventory">
              <template v-if="isCreate">
                Create New Inventory Item
              </template>
              <template v-else>
                Update Inventory Item
              </template>
            </span>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <!-- INPUT: Product code Currently used v-selector to reduce typo probability from user -->
                <v-col cols="6">
                  <v-select
                    no-data-text="No products found"
                    class="required"
                    solo
                    value = "product Code"
                    v-model="productCode"
                    :items="filteredProductList"
                    label="Product Code"
                    item-text="name"
                    item-value="id"
                    :rules="mandatoryRules()"
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
                    v-model.trim="quantity"
                    label="Quantity"
                    :rules="mandatoryRules().concat(quantityRules()).concat(checkQuantityValid())"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Price per item. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model.trim="pricePerItem"
                    label="Price Per Item"
                    :prefix="currency.symbol"
                    :suffix="currency.code"
                    :hint="currency.errorMessage"
                    :rules="smallPriceRules()"
                    outlined
                  />
                </v-col>
                <!-- INPUT: Total Price. Only allows number or '.'but come with 2 digit -->
                <v-col cols="6">
                  <v-text-field
                    v-model.trim="totalPrice"
                    label="Total Price"
                    :prefix="currency.symbol"
                    :suffix="currency.code"
                    :hint="currency.errorMessage"
                    :rules="hugePriceRules()"
                    outlined/>
                </v-col>
                <!-- INPUT: Manufactured. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="manufactured"
                    label="Manufactured"
                    type="date"
                    @input=checkAllDatesValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Sell By. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="sellBy"
                    label="Sell By"
                    type="date"
                    @input=checkAllDatesValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Best Before. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    v-model="bestBefore"
                    label="Best Before"
                    type="date"
                    @input=checkAllDatesValid()
                    outlined/>
                </v-col>
                <!-- INPUT: Expires. Only take in value in dd/mm/yyyy format.-->
                <v-col cols="6">
                  <v-text-field
                    class="required"
                    v-model="expires"
                    label="Expires"
                    type="date"
                    @input=checkAllDatesValid()
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
              @click.prevent="submit">
              <template v-if="isCreate">
                Create
              </template>
              <template v-else>
                Save
              </template>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form>
    </v-dialog>
  </v-row>
</template>

<script>
import { currencyFromCountry } from "@/api/currency";
import {hugePriceRules, mandatoryRules, quantityRules, smallPriceRules} from "@/utils";
import {getProducts} from "@/api/product";
import {createInventoryItem, modifyInventoryItem} from "@/api/inventory";

export default {
  name: 'InventoryItemForm',
  props: {
    previousItem: Object,
    businessId: Number
  },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      valid: false,
      today: new Date(),
      productCode : this.previousItem?.product.id ?? "",
      productList: [],
      quantity : this.previousItem?.quantity ?? "",
      pricePerItem: this.previousItem?.pricePerItem ?? "",
      totalPrice: this.previousItem?.totalPrice ??"",
      manufactured: this.previousItem?.manufactured ?? "",
      manufacturedValid: true,
      sellBy: this.previousItem?.sellBy ?? "",
      sellByValid: true,
      bestBefore: this.previousItem?.bestBefore ?? "",
      bestBeforeValid: true,
      expires: this.previousItem?.expires ?? new Date().toISOString().slice(0,10),
      expiresValid: true,
      datesValid: true,
      productFilter: '',
      minDate: new Date("1500-01-01"),
      maxDate: new Date("5000-01-01"),
      currency: {},
      mandatoryRules: () => mandatoryRules,
      quantityRules: () => quantityRules,
      smallPriceRules: () => smallPriceRules,
      hugePriceRules: () => hugePriceRules,
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
     * Populates the products array for the dropdown select for selecting a product
     * @returns {Promise<void>}
     */
    async fetchProducts() {
      // get the list of products for this business
      const result = await getProducts(this.businessId, null, 10000, 'name', false);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.productList = result.results;
        this.errorMessage = undefined;
      }
    },
    resetSearch: function () {
      this.productFilter = '';
    },
    /**
     * Defines a predicate used for filtering the available products
     * Predicate matches Id, Name, Manufacturer and Description
     * @param product The product to compare
     * @returns {boolean|undefined}
     */
    filterPredicate(product) {
      const filterText = this.productFilter ?? '';
      return product.id.toLowerCase().includes(filterText.toLowerCase()) ||
          product.name.toLowerCase().includes(filterText.toLowerCase()) ||
          product.manufacturer?.toLowerCase().includes(filterText.toLowerCase()) ||
          product.description?.toLowerCase().includes(filterText.toLowerCase());
    },
    /**
     * Called when the form is submitted.
     * Get the attributes from each field and call the api function to either create or modify the inventory item,
     * depending on what pupose the form is being used for.
     * Displays an error message if the api response is an error.
     */
    async submit() {
      this.errorMessage = undefined;
      let quantity;
      try {
        quantity = parseInt(this.quantity);
      } catch (error) {
        this.errorMessage = 'Could not parse field \'Quantity\'';
        return;
      }
      const inventoryItem = {
        productId: this.productCode,
        quantity: quantity,
        pricePerItem: this.pricePerItem.length ? this.pricePerItem : undefined,
        totalPrice: this.totalPrice ? this.totalPrice : undefined,
        manufactured: this.manufactured ? this.manufactured : undefined,
        sellBy: this.sellBy ? this.sellBy : undefined,
        bestBefore: this.bestBefore ? this.bestBefore : undefined,
        expires: this.expires
      };
      let result;
      if (this.isCreate) {
        result = await createInventoryItem(this.businessId, inventoryItem);
      } else {
        result = await modifyInventoryItem(this.businessId, this.previousItem.id, inventoryItem);
      }
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.closeDialog();
      }
    },
    checkAllDatesValid() {
      //checks all the dates are consistent with each other
      this.errorMessage = undefined;
      this.checkManufacturedDateValid();
      this.checkSellByDateValid();
      this.checkBestBeforeDateValid();
      this.checkExpiresDateVaild();
      this.datesValid = this.manufacturedValid && this.sellByValid && this.bestBeforeValid && this.expiresValid;
    },
    checkManufacturedDateValid() {
      //checks manufactured cannot be after today and is before sell by
      let sellByDate = new Date(this.manufactured);
      let manufacturedDate = new Date(this.manufactured);
      this.manufacturedValid = false;
      if (manufacturedDate < this.minDate || manufacturedDate > this.maxDate) {
        this.errorMessage = "The manufactured date cannot be before 1500 AD or after 5000 AD";
      } else if (manufacturedDate > this.today) {
        this.errorMessage = "The manufactured date is after today!";
      } else if (manufacturedDate > sellByDate) {
        this.errorMessage = "The manufactured date cannot be after the sell by date!";
      } else {
        this.manufacturedValid = true;
      }
    },
    checkSellByDateValid() {
      //checks sell by date cannot be before today and is after manufactured and before best before
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      let manufacturedDate = new Date(this.manufactured);
      this.sellByValid = false;
      if (sellByDate < this.minDate || sellByDate > this.maxDate) {
        this.errorMessage = "The sell by date cannot be before 1500 AD or after 5000 AD";
      } else if (sellByDate < this.today) {
        this.errorMessage = "The sell by date is before today!";
      } else if (sellByDate < manufacturedDate) {
        this.errorMessage = "The sell by date cannot be before the manufactured date!";
      } else if (sellByDate > bestBeforeDate) {
        this.errorMessage = "The sell by date cannot be after the best before date!";
      } else if (sellByDate > expiresDate) {
        this.errorMessage = "The sell by date cannot be after the expiry date!";
      } else {
        this.sellByValid = true;
      }
    },
    checkBestBeforeDateValid() {
      //checks best before date cannot be before today and is after sell by date
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      this.bestBeforeValid = false;
      if (bestBeforeDate < this.minDate || bestBeforeDate > this.maxDate) {
        this.errorMessage = "The best before date cannot be before 1500 AD or after 5000 AD";
      } else if (bestBeforeDate < this.today) {
        this.errorMessage = "The best before date is before today!";
      } else if (bestBeforeDate < sellByDate) {
        this.errorMessage = "The best before date cannot be before the sell by date!";
      } else if (bestBeforeDate > expiresDate) {
        this.errorMessage = "The best before date cannot be after the expires date!";
      } else {
        this.bestBeforeValid = true;
      }
    },
    checkExpiresDateVaild() {
      //checks expires date cannot be before today and is after best before date
      let expiresDate = new Date(this.expires);
      let bestBeforeDate = new Date(this.bestBefore);
      let sellByDate = new Date(this.sellBy);
      this.expiresValid = false;
      if (!this.expires) {
        this.errorMessage = "An expiry date must be entered";
      } else if (expiresDate < this.minDate || expiresDate > this.maxDate) {
        this.errorMessage = "The expires date cannot be before 1500 AD or after 5000 AD";
      } else if (expiresDate < this.today) {
        this.errorMessage = "The expires date is before today!";
      } else if (expiresDate < bestBeforeDate) {
        this.errorMessage = "The expires date cannot be before the best before date!";
      } else if (sellByDate > expiresDate) {
        this.errorMessage = "The expires date cannot be before the sell by date!";
      } else {
        this.expiresValid = true;
      }
    },
    /**
     * Checks that the quantity is greater than or equal to the amount that has already been used in sale listings if the
     * form is being used to modify an inventory item.
     */
    checkQuantityValid() {
      if (this.isCreate) {
        return true;
      }
      return this.quantity >= (this.previousItem.quantity - this.previousItem.remainingQuantity) ||
      `Must be at least ${this.previousItem.quantity - this.previousItem.remainingQuantity}`;
    },
    /**
     * Call the currency API to get the currency symbol and code from the country of sale of the product.
     */
    async fetchCurrency() {
      if (this.productCode && this.productList.length > 0) {
        const product = this.productList.filter(p => p.id === this.productCode)[0];
        this.currency = await currencyFromCountry(product.countryOfSale);
      } else {
        this.currency = {errorMessage: "Currency not available"};
      }
    }
  },
  computed: {
    /**
     * Filters the list of products based on the value of the search term
     * value must be passed to ensure products are refreshed when input is cleared
     */
    filteredProductList() {
      return this.productList.filter(x => this.filterPredicate(x));
    },
    /**
     * Returns true if this form is for creating an inventory item, false if it is for modifying an inventory item.
     */
    isCreate() {
      return this.previousItem === undefined;
    },
  },
  async created() {
    await this.fetchProducts();
    this.fetchCurrency();
  },
  /**
   * When the product code changes, update the currency as it depends on the product.
   */
  watch: {
    productCode: function () {
      this.fetchCurrency();
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

.create-inventory {
  color: var(--v-primary-base);
  font-weight: bolder;
}
</style>