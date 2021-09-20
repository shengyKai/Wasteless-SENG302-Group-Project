<template>
  <div>
    <v-row class="mt-5">
      <v-col class="align-self-center shrink" no-gutters>
        <v-tooltip right>
          <template v-slot:activator="{ on, attrs }">
            <v-chip
              class="mt-2"
              color="success"
              outlined
              v-bind="attrs"
              v-on="on"
            >
              <v-icon center>
                mdi-upload
              </v-icon>
            </v-chip>
          </template>
          <span>Click to upload an image</span>
        </v-tooltip>
        <v-tooltip right>
          <template v-slot:activator="{ on, attrs }">
            <v-chip
              class="mt-2"
              color="success"
              outlined
              v-bind="attrs"
              v-on="on"
            >
              <v-icon center>
                mdi-trash-can
              </v-icon>
            </v-chip>
          </template>
          <span>Click to delete an image</span>
        </v-tooltip>
        <v-tooltip right>
          <template v-slot:activator="{ on, attrs }">
            <v-chip
              class="mt-2"
              color="success"
              outlined
              v-bind="attrs"
              v-on="on"
            >
              <v-icon center>
                mdi-image-edit
              </v-icon>
            </v-chip>
          </template>
          <span>Click to set image as thumbnail primary</span>
        </v-tooltip>
      </v-col>
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
  </div>
</template>

<script>
export default {
  data: () => ({
    model: 0,
    colors: [
      'primary',
      'secondary',
      'yellow darken-2',
      'red',
      'orange',
    ],
  }),
};
</script>
