<template>
  <v-dialog v-model="showDialog" persistent max-width="500px">
    <v-form>
      <v-card>
        <v-card-title>
          Upload Business Image
        </v-card-title>
        <v-card-text>
          <ImageSelector v-model="file"/>
        </v-card-text>
        <v-card-actions>
          <v-spacer/>
          <!-- Current error message -->
          <div class="error-text" v-if="errorMessage !== undefined"> {{ errorMessage }} </div>
          <v-spacer/>
          <v-btn
            color="primary"
            text
            @click="closeForm">
            Close
          </v-btn>
          <v-btn
            type="submit"
            color="primary"
            :disabled="!file"
            :loading="isLoading"
            @click.prevent="uploadImage">
            Upload
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<script>
import ImageSelector from "@/components/utils/ImageSelector";
import {uploadBusinessImage} from "@/api/internal";

export default {
  name: "BusinessImageUploader",
  components: {ImageSelector},
  props: {
    //we need these two values to update the product image accordingly
    businessId: Number,
    //the value here refers to the parent component in ProductCatalogueItem
    value: Boolean
  },
  data() {
    return {
      file: undefined,
      isLoading: false,
      errorMessage: undefined,
    };
  },
  methods: {
    /**
     * Handler for the "create" button
     * This will trigger a call to the add product image endpoint and close the dialog if successful
     */
    async uploadImage() {
      this.isLoading = true;
      this.errorMessage = undefined;

      let response = await uploadBusinessImage(this.businessId, this.file);

      this.isLoading = false;
      if (response !== undefined) {
        this.errorMessage = response;
        return;
      }
      this.showDialog = false;
      this.file = undefined;
      this.$emit('image-added');
    },
    /**
     * Closes the dialog and clears any selected files
     */
    closeForm() {
      this.showDialog = false;
      this.file = undefined;
    },
  },
  computed: {
    //need to use computed property in child component to track changes
    showDialog: {
      //gets the value of the showImageUploaderForm
      get() {
        return this.value;
      },
      set (value) {
        this.$emit('input', value);
      },
    }
  }
};
</script>

<style scoped>

</style>