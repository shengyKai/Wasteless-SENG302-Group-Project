<template>
  <div>
    <v-row class="mt-5">
      <v-col>
        <v-carousel v-model="model">
          <v-carousel-item
            v-for="(color, i) in colors"
            :key="color"
          >
            <v-sheet
              :color="color"
              height="100%"
              tile
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
              <v-row
                class="fill-height"
                align="center"
                justify="center"
              >
                <div class="text-h2">
                  Slide {{ i + 1 }}
                </div>
              </v-row>
            </v-sheet>
          </v-carousel-item>
        </v-carousel>
      </v-col>
    </v-row>
    <ImageUploader
      v-model="image"
      v-if="showImageUploader"
      @closeDialog="upload"
    />
  </div>
</template>

<script>
import ImageUploader from "@/components/utils/ImageUploader";

export default {
  components: {
    ImageUploader,
  },
  data: () => ({
    model: 0,
    colors: [
      'primary',
      'secondary',
      'yellow darken-2',
      'red',
      'orange',
    ],
    showImageUploader: false,
    image: undefined,
  }),
  methods: {
    upload() {
      this.showImageUploader = false;
    }
  }
};
</script>
