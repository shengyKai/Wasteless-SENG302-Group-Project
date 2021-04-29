<template>
  <div>
    <v-file-input v-model="file" filled prepend-icon="mdi-camera"/>
    <v-card
      :elevation="isDragging ? 4 : 2"
      class="preview-window"
      @dragover="onDragOver"
      @dragleave="isDragging = false"
      @drop="onDrop"
    >
      <img v-if="url" :src="url" class="preview-image">
      <template v-else>
        <div>
          Drag a file
        </div>
        <v-icon>mdi-upload</v-icon>
      </template>
    </v-card>
  </div>
</template>

<script>
export default {
  name: 'ImageUploader',
  data() {
    return {
      file: null,
      isDragging: false,
    };
  },
  methods: {
    onDragOver(event) {
      let data = event.dataTransfer;
      if (data.items.length !== 1) return;

      let item = data.items[0];
      if (item.kind !== 'file' || !item.type.match('^image/')) return;

      data.effectAllowed = 'copy';
      this.isDragging = true;

      event.preventDefault();
    },
    onDrop(event) {
      this.file = event.dataTransfer.files[0];

      this.isDragging = false;
      event.preventDefault();
    }
  },

  computed: {
    url() {
      if (this.file === null) return null;
      return URL.createObjectURL(this.file);
    }
  }
};
</script>

<style>
.preview-window {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 400px;
  height: 400px;
}

.preview-image {
  width: 100%;
  object-fit: contain;
}
</style>