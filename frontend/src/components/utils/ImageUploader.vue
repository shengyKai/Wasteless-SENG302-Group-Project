<template>
  <v-dialog v-model="visible" persistent max-width="500px">
    <v-form>
      <v-card>
        <v-card-title>
          Upload Product Image
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row justify="center">
              <div v-if="url" class="preview-container">
                <img
                  alt="Uploaded Image"
                  :src="url"
                  class="image-preview"
                  @dragover="onDragOver"
                  @dragleave="isDragging = false"
                  @drop="onDrop">
                <v-btn icon class="remove-image" @click="file=undefined" color="error">
                  <v-icon>mdi-close</v-icon>
                </v-btn>
              </div>
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
        </v-card-text>
        <v-card-actions>
          <v-spacer/>
          <div class="error-text" v-if="errorMessage !== undefined"> {{ errorMessage }} </div>
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
            :disabled="!file"
            :loading="isLoading"
            @click.prevent="uploadImage">
            Create
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </v-dialog>
</template>

<script>
export default {
  name: "ImageUploader",
  data() {
    return {
      file: undefined,
      isDragging: false,
      visible: true,
      errorMessage: undefined,
      isLoading: false,
    };
  },
  methods: {
    onDragOver(event) {
      let data = event.dataTransfer;
      if (data.items.length !== 1) return;

      let item = data.items[0];
      if (item.kind !== "file" || !item.type.match("^image/")) return;

      data.effectAllowed = "copy";
      this.isDragging = true;

      event.preventDefault();
    },
    onDrop(event) {
      this.file = event.dataTransfer.files[0];

      this.isDragging = false;
      event.preventDefault();
    },
    openFileDialog() {
      let input = document.createElement("input");
      input.setAttribute('type', 'file');
      input.setAttribute('accept', 'image/*');
      // add onchange handler if you wish to get the file :)

      input.onchange = () => {
        const files = input.files;
        if (files.length !== 1) return;
        const file = files[0];
        if (!file.type.match('^image/')) return;

        this.file = file;
      };

      input.click();
    },

    uploadImage() {
      console.log('Uploading!');
    },

    closeDialog() {
      this.$emit('closeDialog');
    }
  },

  computed: {
    url() {
      if (this.file === undefined) return undefined;
      return URL.createObjectURL(this.file);
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