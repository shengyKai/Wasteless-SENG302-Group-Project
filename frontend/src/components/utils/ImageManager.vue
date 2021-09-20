<template>
  <div>
    <v-row>
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
    </v-row>
    <v-row no-gutters>
      <v-col>
        <v-expand-transition>
          <v-card
            v-show="expand"
            max-width="1200"
            class="mx-auto"
          >
            <div class="font-weight-medium">
              <span class="ml-4">
                Change your Tag:
              </span>
            </div>
            <!--  Content that run through a loop of colours which at the same time set the colour of the chip
              Make the code more maintainable as it will be easy to modify colour in future and get the index
              Trigger a tagNotification when the chip is clicked (will take the colour as param)
        -->
            <div class="ml-3">
              <v-chip
                class="ma-1"
                v-for="colour in colours"
                :key=colour
                :color="colour"
                label
                text-color="white"
                @click="tagNotification(colour)"
              >
                <v-icon left>
                  mdi-label
                </v-icon>
              </v-chip>
            </div>
          </v-card>
        </v-expand-transition>
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
