<template>
  <div>
    <v-row class="mt-5">
      <v-col>
        <v-carousel v-if="images.length > 0" v-model="model">
          <v-carousel-item
            v-for="image in images"
            :key="image"
            :src="imageUrl(image.filename)"
            contain
          >
            <v-row>
              <v-col class="text-right">
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            outlined
                            class="ma-2"
                            color="primary"
                            v-bind="attrs"
                            v-on="on">
                      mdi-upload
                    </v-icon>
                  </template>
                  <span>Upload a new image</span>
                </v-tooltip>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on, attrs }">
                    <v-icon center
                            outlined
                            class="ma-2"
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
                            class="ma-2"
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
        <v-carousel
          v-else
          v-model="model"
        >
          <v-sheet
            color="primary"
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
                          color="secondary"
                          v-bind="attrs"
                          v-on="on"
                          x-large>
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
  </div>
</template>

<script>
import {imageSrcFromFilename} from "@/utils";

export default {
  props: {
    images: Array
  },
  data: () => ({
    model: 0,
  }),
  methods: {
    imageUrl(filename) {
      return imageSrcFromFilename(filename);
    }
  }
};
</script>
