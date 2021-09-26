<template>
  <v-dialog v-model="showDialog" persistent max-width="500px">
    <v-form>
      <v-card>
        <v-card-title>
          Upload Image
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
import ImageSelector from "@/components/image/ImageSelector";
import { uploadImage } from "@/api/images";

export default {
  name: "ImageUploader",
  components: { ImageSelector },
  props: {
    value: undefined,
  },
  data() {
    return {
      isLoading: false,
      errorMessage: undefined,
      showDialog: true,
      file: undefined
    };
  },
  methods: {
    /**
     * Uploads the image and if successful closes the dialog form
     */
    async uploadImage() {
      const response = await uploadImage(this.file);
      if (typeof response === 'string') {
        this.errorMessage = response;
      } else {
        this.image = response;
        this.$emit('upload');
      }
    },
    /**
     * Closes the dialog and clears any selected files
     */
    closeForm() {
      this.file = undefined;
      this.$emit('closeDialog');
    },
  },
  computed: {
    image: {
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