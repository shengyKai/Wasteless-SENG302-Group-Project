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
                <v-col>
                  <v-text-field
                    v-model="manufacturer"
                    label="Manufacturer"
                    :rules="maxCharRules"
                    outlined
                  />
                </v-col>
                <v-col>
                  <v-text-field
                    v-model="price"
                    label="Recommended Retail Price"
                    :rules="priceRules"
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
              @click="closeDialog"
              Close
            />
            <v-btn
              type="submit"
              color="primary"
              :disabled="!valid"
              @click="createProduct"
              Create
            />
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
      product: '',
      description: '',
      manufacturer: '',
      price: '',
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
        field => /^\d{1,5}(\.\d{2})?$/.test(field) || 'Must be a valid price'
      ]
    };
  },
  methods: {
    createProduct() {
      this.$router.push('/businesses/1/products');
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

.create-product {
  color: #558b2f; /* TODO Set this to primary colour variable */
  font-weight: bolder;
}
</style>