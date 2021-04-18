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
                <v-col cols="6">
                  <v-text-field
                    v-model="price"
                    label="Recommended Retail Price"
                    :rules="priceRules"
                    outlined
                  />
                </v-col>
                <v-col cols="6">
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
              @click="createProduct">
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
  name:'CreateProduct',
  data() {
    return {
      dialog: true,
      dateMenu: false,
      product: '',
      description: '',
      expiryDate: '',
      manufacturer: '',
      price: '',
      quantity: '',
      productCode: '',
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
  methods: {
    createProduct() {
      //TODO merge with backend
      //this.$router.push('/businesses/1/products');
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