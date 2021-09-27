<template>
  <div>
    <v-row class="mt-5">
      <v-col>
        <v-carousel
          v-if="outputImages.length > 0"
          v-model="model"
        >
          <v-carousel-item
            v-for="(image, enumerator) in outputImages"
            :key="enumerator"
            :src="imageUrl(image.filename)"
            contain
          >
            <v-row>
              <v-col class="text-right">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            ref="upload"
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
                            ref="trashCan"
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
                            ref="makePrimary"
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
                          ref="uploadWithNoImages"
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
<<<<<<< HEAD
    colors: [
      'yellow darken-2',
      'secondary',
      'grey',
      'red',
      'orange',
    ],
=======
    showImageUploader: false,
    uploadedImage: undefined,
>>>>>>> dev
  }),
  computed: {
    /**
     * An cloned array of images from the images prop. This is needed because we cannot mutate props and we want to show a carousel of
     * images which contains the images that are both tied and pending to be tied to the entity.
     */
    outputImages() {
      return Array.from(this.images);
    }
  },
  methods: {
    /**
     * Method to push the uploaded image to the end of the image array, so that it can be shown to the user.
     * Emits an event "updateImages" along with the images which are uploaded to the parent to submit the form for modification.
     * Takes in a boolean isActionForImageUpload, to identify whether the upload method refers to a close dialog action or a
     * action to upload images, since they are both being called using the same emit event from ImageUploader
     */
    upload(isActionForImageUpload) {
      if (isActionForImageUpload) {
        this.outputImages.push(this.uploadedImage);
        this.$emit("updateImages", this.outputImages);
      }
      this.showImageUploader = false;
    },
    /**
     * Method to call another method to get the image source from the image file name
     */
    imageUrl(filename) {
      return imageSrcFromFilename(filename);
    },
  },
};
</script>

<style scoped>
.img-button {
  text-shadow: 0px 0px 14px #000000;
}
</style>
