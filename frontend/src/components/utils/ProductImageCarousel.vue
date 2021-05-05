<template>
  <!-- use the v-dialog to show a pop up for the carousel -->
  <v-dialog v-model="dialog">
    <template v-slot:activator="{ on, attrs }">
      <!-- put an image over a link, such that now the image will be clickable to activate the pop up dialog -->
      <!--  v-bind="attrs" v-on="on" allows the v-dialog to use this link as the activator for the dialog box -->
      <a v-bind="attrs" v-on="on">
        <!-- productImages[0] will be the primary image for the product. -->
        <v-img height="100%" :src="productImages[0].filename"/>
      </a>
    </template>
    <template>
      <v-carousel
        v-model="carouselItem"
        show-arrows-on-hover
        hide-delimiters
      >
        <!-- iterate through each photo in productImages -->
        <v-carousel-item v-for="(item, i) in productImages" :key="i" :src="item.filename" >
          <v-tooltip bottom >
            <template #activator="{ on: tooltip}">
              <v-btn
                icon
                v-if="i !== 0"
                color="primary"
                v-on="{...tooltip}"
                @click="makeImagePrimary(item.id)"
                ref="makePrimaryImageButton"
              >
                <v-icon>mdi-eye-plus</v-icon>
              </v-btn>
            </template>
            <span> Make Primary Image </span>
          </v-tooltip>
          <v-tooltip bottom >
            <template #activator="{ on: tooltip}">
              <v-btn
                icon
                color="error"
                v-on="{...tooltip}"
                @click="deleteImage(item.id)"
                ref="deleteImageButton"
              >
                <v-icon>mdi-delete</v-icon>
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
export default {
  name: "ProductImageCarousel",
  //pass in productImages from parent compoenent
  props: ["productImages", "productId"],
  data() {
    return {
      carouselItem: 0,
      // if dialog is false, the popup does not appear.
      dialog: false
    };
  },
  methods: {
    /**
     * Sets the currently selected image as the primary image.
     * @param imageId Id of the currently selected image
     */
    makeImagePrimary(imageId) {
      this.$emit('change-primary-image', imageId);
    },
    /**
     * Deletes the provided image
     * @param imageId Image to delete
     */
    deleteImage(imageId) {
      this.$emit('delete-image', imageId);
    }
  },
};
</script>
