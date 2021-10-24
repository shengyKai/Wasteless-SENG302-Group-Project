<template>
  <!-- use the v-dialog to show a pop up for the carousel -->
  <v-dialog v-model="dialog" width="min(90vh, 100vw)" v-if="imagesList && imagesList.length > 0">
    <template v-slot:activator="{ on, attrs }">
      <!-- put an image over a link, such that now the image will be clickable to activate the pop up dialog -->
      <!--  The v-bind to attrs allows the v-dialog to use this link as the activator for the dialog box -->
      <a v-bind="attrs" v-on="on">
        <!-- imagesList[0] will be the primary image. -->
        <v-img class="text-right" height="200px" :src="imageUrl(imagesList[0].filename)">
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-icon
                left
                color="green"
                class="shadow-icon mt-1"
                v-bind="attrs"
                v-on="on"
              >
                mdi-arrow-expand
              </v-icon>
            </template>
            <span>View more images</span>
          </v-tooltip>
        </v-img>
      </a>
    </template>
    <template>
      <v-carousel
        v-model="carouselItem"
        show-arrows-on-hover
        hide-delimiters
        height="auto"
      >
        <!-- iterate through each photo in imagesList -->
        <v-carousel-item
          v-for="(item, i) in imagesList"
          :key="i"
          :src="imageUrl(imagesList[i].filename)"
        >
          <v-tooltip bottom>
            <template #activator="{ on: tooltip }">
              <v-btn
                icon
                v-if="i !== 0 && showMakePrimary"
                color="primary"
                v-on="{ ...tooltip }"
                @click="makeImagePrimary(item.id)"
                ref="makePrimaryImageButton"
                x-large
              >
                <v-icon class="shadow-icon">mdi-eye-plus</v-icon>
              </v-btn>
            </template>
            <span> Make Primary Image </span>
          </v-tooltip>
          <v-tooltip bottom>
            <template #activator="{ on: tooltip }">
              <v-btn
                icon
                v-if="showDelete"
                color="error"
                v-on="{ ...tooltip }"
                @click="deleteImage(item.id)"
                ref="deleteImageButton"
              >
                <v-icon class="shadow-icon">mdi-delete</v-icon>
              </v-btn>
            </template>
            <span> Delete Image </span>
          </v-tooltip>
        </v-carousel-item>
      </v-carousel>
    </template>
  </v-dialog>
</template>
<script>
import {imageSrcFromFilename} from "@/utils";

export default {
  name: "ImageCarousel",
  props: {
    /**
     * Array of image objects to display
     */
    imagesList: Array,
    /**
     * Whether to show the make primary and delete image
     */
    showMakePrimary: Boolean,
    showDelete: Boolean,
  },
  data() {
    return {
      carouselItem: 0,
      // if dialog is false, the popup does not appear.
      dialog: false,
    };
  },
  methods: {
    /**
     * Sets the currently selected image as the primary image.
     * @param imageId Id of the currently selected image
     */
    makeImagePrimary(imageId) {
      this.$emit("change-primary-image", imageId);
    },
    /**
     * Deletes the provided image
     * @param imageId Image to delete
     */
    deleteImage(imageId) {
      this.$emit("delete-image", imageId);
    },
    forceClose() {
      this.dialog = false;
    },
    imageUrl(filename) {
      return imageSrcFromFilename(filename);
    }
  },
};
</script>

<style scoped>
.test-class {
  display: none;
}

.thingy {
  display: none;
}

.shadow-icon {
  text-shadow: 0px 0px 14px #000000;
}
</style>