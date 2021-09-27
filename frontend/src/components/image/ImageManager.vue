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
                            v-on="on"
                            @click="deleteImage(image)">
                      mdi-trash-can
                    </v-icon>
                  </template>
                  <span>Delete the current image</span>
                </v-tooltip>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            outlined
                            v-if="enumerator !== 0"
                            ref="makePrimary"
                            class="ma-2 img-button"
                            color="primary"
                            v-bind="attrs"
                            v-on="on"
                            @click="makeImagePrimary(image)">
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
      v-if="showImageUploader"
      @upload="upload"
      @closeDialog="showImageUploader=false"
    />
  </div>
</template>

<script>
import ImageUploader from "@/components/image/ImageUploader";
import {imageSrcFromFilename} from "@/utils";

export default {
  components: {
    ImageUploader,
  },
  props: {
    value: {
      type: Array,
      required: true
    }
  },
  data: function() {
    return {
      model: 0,
      showImageUploader: false,
      outputImages: this.value,
    };
  },
  methods: {
    /**
     * Method to push the uploaded image to the end of the image array, so that it can be shown to the user.
     * @param image The image to be added to image list
     */
    upload(image) {
      this.outputImages.push(image);
    },
    /**
     * Assigns the given image as the primary image
     * More specifically, assigns the given image as the image in the first index of the image array.
     * @param image Image to be assigned primary image
     */
    makeImagePrimary(image) {
      this.deleteImage(image);
      this.outputImages.unshift(image);
    },
    /**
     * Deletes an image by removing it from the list of images
     * @param image Image to be removed
     */
    deleteImage(image) {
      const index = this.outputImages.indexOf(image);
      if (index !== -1) {
        this.outputImages.splice(index, 1);
      }
    },
    /**
     * Method to call another method to get the image source from the image file name
     */
    imageUrl(filename) {
      return imageSrcFromFilename(filename);
    },
  },
  watch: {
    outputImages: function() {
      this.$emit('input', this.outputImages);
    }
  }
};
</script>

<style scoped>
.img-button {
  text-shadow: 0px 0px 14px #000000;
}
</style>
