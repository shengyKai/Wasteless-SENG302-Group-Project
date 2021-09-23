<template>
  <div>
    <v-row class="mt-5">
      <v-col>
        <v-carousel
          v-if="toBeSubmittedImages.length > 0"
          v-model="model"
        >
          <v-carousel-item
            v-for="(image, enumerator) in toBeSubmittedImages"
            :key="enumerator"
            :src="imageUrl(image.filename)"
            contain
          >
            <v-row>
              <v-col class="text-right">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            outlined
                            class="ma-2 img-button"
                            color="primary"
                            v-bind="attrs"
                            v-on="on"
                            @click="showImageUploader = true">
                      mdi-upload
                    </v-icon>
                  </template>
                  <span>Upload a new image</span>
                </v-tooltip>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            outlined
                            class="ma-2 img-button"
                            color="primary"
                            v-bind="attrs"
                            v-on="on">
                      mdi-trash-can
                    </v-icon>
                  </template>
                  <span>Delete the current image</span>
                </v-tooltip>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            outlined
                            class="ma-2 img-button"
                            color="primary"
                            v-bind="attrs"
                            v-on="on">
                      mdi-image-edit
                    </v-icon>
                  </template>
                  <span>Set current image as primary thumbnail</span>
                </v-tooltip>
              </v-col>
            </v-row>
          </v-carousel-item>
        </v-carousel>
        <!-- This is needed because the above does not account for the scenario that the associated entity does not have
        images related to it, of which the upload button will not be present. -->
        <v-carousel
          v-else
          :show-arrows="false"
          v-model="model"
          :hide-delimiter-background="true"
          :max="4"
        >
          <v-sheet
            color="secondary"
            height="100%"
            tile
          >
            <v-row
              class="fill-height"
              justify="center"
              align="center"
            >
              <v-tooltip bottom>
                <template v-slot:activator="{ on, attrs }">
                  <v-icon center
                          outlined
                          class="ma-2"
                          color="primary"
                          v-bind="attrs"
                          v-on="on"
                          size="80"
                          @click="showImageUploader = true">
                    mdi-upload
                  </v-icon>
                </template>
                <span>Upload a new image</span>
              </v-tooltip>
            </v-row>
          </v-sheet>
        </v-carousel>
      </v-col>
    </v-row>
    <ImageUploader
      v-model="uploadedImage"
      v-if="showImageUploader"
      @closeDialog="upload"
    />
  </div>
</template>

<script>
import ImageUploader from "@/components/utils/ImageUploader";
import {imageSrcFromFilename} from "@/utils";

export default {
  components: {
    ImageUploader,
  },
  props: {
    images: Array
  },
  data: () => ({
    model: 0,
    showImageUploader: false,
    uploadedImage: undefined,
    toBeSubmittedImages: []
  }),
  methods: {
    /**
     * Method to push the uploaded image to the end of the image array, so that it can be shown to the user.
     * Emits an event "updateImages" along with the images which are uploaded to the parent to submit the form for modification.
     */
    upload() {
      this.toBeSubmittedImages.push(this.uploadedImage);
      this.$emit("updateImages", this.toBeSubmittedImages);
      this.showImageUploader = false;
    },
    /**
     * Method to call another method to get the image source from the image file name
     */
    imageUrl(filename) {
      return imageSrcFromFilename(filename);
    },
  },
  mounted() {
    // Cloning because we cannot mutate a prop. Cant do this in the data() section for some reason.
    this.toBeSubmittedImages = Array.from(this.images);
  }
};
</script>

<style scoped>
.img-button {
  text-shadow: 0px 0px 14px #000000;
}
</style>
