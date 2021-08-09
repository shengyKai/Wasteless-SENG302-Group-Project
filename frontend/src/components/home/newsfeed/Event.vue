<template>
  <div>
    <div class="delete">
      <v-icon class="deleteButton"
              color="red"
              @click.stop="removeNotification"
      >
        mdi-trash-can
      </v-icon>
      <div class="deletion-error">
        {{ errorMessage }}
      </div>
    </div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-subtitle>
      {{ date }}, {{ time }}
    </v-card-subtitle>
    <slot/>
    <!-- Expansion panel for user to view their option about the available tag to choose from  -->
    <v-expansion-panels>
      <v-expansion-panel>
        <!-- The persistent chip that shows the tag for the message (default will be no colour) -->
        <v-expansion-panel-header
          label
        >
          <div>
            <v-chip
              color="pink"
              label
              text-color="white"
            >
              <v-icon left>
                mdi-label
              </v-icon>
              Current Tag
            </v-chip>
          </div>
        </v-expansion-panel-header>
        <!--  The expansion panel's content that run through a loop of colours which at the same time set the colour of the chip
              Make the code more maintainable as it will be easy to modify colour in future and get the index
              Trigger a method when the chip is clicked (will use the index to trigger)
        -->
        <v-expansion-panel-content>
          <div>
            Change your Tag:
          </div>
          <v-chip
            class="mr-1"
            v-for="colour in colours"
            :key=colour
            :color="colour"
            label
            text-color="white"
            @click="changeTag"
          >
            <v-icon left>
              mdi-label
            </v-icon>
            Tag
          </v-chip>
        </v-expansion-panel-content>
      </v-expansion-panel>
    </v-expansion-panels>

    <v-row
      justify="start"
      style="min-height: 10px;"
    >
      <v-col class="shrink">
        <v-chip
          class="ma-2"
          color="primary"
          @click="expand = !expand"
          label
          text-color="white"
        >
          <v-icon left>
            mdi-label
          </v-icon>
          Current Tag
        </v-chip>
        <v-expand-transition>
          <v-card
            v-show="expand"
            height="70"
            width="1000"
            class="mx-auto"
          >
            <div class="font-weight-medium">
              Change your Tag:
            </div>
            <v-chip
              class="ma-1"
              v-for="colour in colours"
              :key=colour
              :color="colour"
              label
              text-color="white"
              @click="changeTag"
            >
              <v-icon left>
                mdi-label
              </v-icon>
              Tag
            </v-chip>
          </v-card>
        </v-expand-transition>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import { formatDate } from '@/utils';
import {deleteNotification} from "@/api/internal";

export default {
  name: 'Event',
  props: {
    title: {
      type: String,
      required: true,
    },
    event: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      expand: false,
      colours: ['none', 'red', 'orange', 'yellow', 'green', 'blue', 'purple'],
      errorMessage: undefined
    };
  },
  computed: {
    /**
     * The time the event was created in a displayable format (hh:mm).
     */
    time() {
      let fullTime = new Date(this.event.created).toTimeString().split(' ')[0];
      return fullTime.split(':').splice(0, 2).join(':');
    },
    /**
     * The date the event was created in a displayable format.
     */
    date() {
      return formatDate(this.event.created);
    },
  },
  methods: {
    async removeNotification() {
      const result = await deleteNotification(this.event.id);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.$store.commit('removeEvent', this.event.id);
      }
    },
    /**
     * This should be a method that allows user to change their current tag colour into the desired colour
     * Currently only alert `changing tag`, functionality will be implemented in another task
     */
    changeTag () {
      alert('Changing Tag...');
    },
  }
};
</script>

<style>
.delete {
  float: right;
  margin: 0.25em;
}
.deleteButton {
  float: right;
  margin: 0.25em;
}
.deletion-error {
  color: red;
}
</style>