<template>
  <v-container>
    <v-row justify="center">
      <!-- The previewed image, if available -->
      <div v-if="url" class="preview-container">
        <img
          alt="Uploaded Image"
          :src="url"
          class="image-preview"
          @dragover="onDragOver"
          @dragleave="onDragLeave"
          @drop="onDrop">
        <!-- Button for removing an image -->
        <v-btn icon class="remove-image" @click="file=undefined" color="error">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </div>
      <!-- The drag image / submit image prompt window -->
      <v-card v-else
              :elevation="isDragging ? 4 : 2"
              @dragover="onDragOver"
              @dragleave="isDragging = false"
              @drop="onDrop"
      >
        <div class="preview-window">
          <v-icon x-large>mdi-upload</v-icon>
          <div>
            <v-btn small @click="openFileDialog">Select</v-btn> image or drag image here
          </div>
        </div>
      </v-card>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: "ImageSelector",
  data() {
    return {
      isDragging: false,
      errorMessage: undefined,
      isLoading: false,
    };
  },
  props: {
    value: undefined,
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
  },
  computed: {
    url() {
      if (this.file === undefined) return undefined;
      return URL.createObjectURL(this.file);
    },
    file: {
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