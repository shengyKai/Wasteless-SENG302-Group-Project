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

export default {
  name: "BusinessImageUploader",
  components: {ImageSelector},
  props: {
    value: undefined,
  },
  data() {
    return {
      isLoading: false,
      errorMessage: undefined,
      showDialog: true
    };
  },
  methods: {
    /*
     * Close the dialog and emit message to the parent to add file to uploaded images.
     */
    uploadImage() {
      this.$emit('uploadImage');
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
    file: {
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