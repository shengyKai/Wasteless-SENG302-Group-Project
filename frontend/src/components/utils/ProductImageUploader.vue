<template>
  <v-dialog v-model="showDialog" persistent max-width="500px">
    <v-form>
      <v-card>
        <v-card-title>
          Upload Product Image
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
import {uploadProductImage} from "@/api/product";
export default {
  name: "ProductImageUploader",
  components: {ImageSelector},
  props: {
    //we need these two values to update the product image accordingly
    businessId: Number,
    productCode: String,
    //the value here refers to the parent component in ProductCatalogueItem
    value: Boolean
  },
  data() {
    return {
      file: undefined,
      isDragging: false,
      errorMessage: undefined,
      isLoading: false,
    };
  },
  methods: {
    /**
     * Handler for a dragged entity leaving the target region
     */
    onDragLeave() {
      this.isDragging = false;
    },
    /**
     * Handler for a dragged entity entering the target region
     */
    onDragOver(event) {
      let data = event.dataTransfer;
      if (data.items.length !== 1) return;

      let item = data.items[0];
      if (item.kind !== "file" || !item.type.match("^image/")) return;

      this.isDragging = true;
      event.preventDefault();
    },
    /**
     * Handler for a dragged file being dropped into this component
     */
    onDrop(event) {
      this.file = event.dataTransfer.files[0];

      this.isDragging = false;
      event.preventDefault();
    },
    /**
     * Handler for when the submit button is clicked.
     * This will result in a dialog being created for selecting a file
     */
    openFileDialog() {
      let input = document.createElement("input");
      input.setAttribute('type', 'file');
      input.setAttribute('accept', 'image/*');

      input.onchange = () => {
        const files = input.files;
        if (files.length !== 1) return;
        const file = files[0];
        if (!file.type.match('^image/')) return;

        this.file = file;
      };

      input.click();
    },
    closeForm() {
      this.showDialog = false;
      this.file = undefined;
    },
    /**
     * Handler for the "create" button
     * This will trigger a call to the add product image endpoint and close the dialog if successful
     */
    async uploadImage() {
      this.isLoading = true;
      this.errorMessage = undefined;

      let response = await uploadProductImage(this.businessId, this.productCode, this.file);

      this.isLoading = false;
      if (response !== undefined) {
        this.errorMessage = response;
        return;
      }
      this.showDialog = false;
      this.file = undefined;
      this.$emit('image-added');
    }
  },

  computed: {
    url() {
      if (this.file === undefined) return undefined;
      return URL.createObjectURL(this.file);
    },
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
  },
};
</script>

<style scoped>
.file-input {
  max-width: 400px;
}

.preview-container {
  position: relative;
}

.image-preview {
  width: 400px;
  height: auto;
}

.remove-image {
  position: absolute;
  right: 0px;
  top: 0px;
}

.preview-window {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 400px;
  height: 400px;
}
</style>