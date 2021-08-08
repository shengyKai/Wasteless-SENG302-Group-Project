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

    <v-expansion-panels>
      <v-expansion-panel>

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
        <v-expansion-panel-content>
          <div>
            Change your Tag:
          </div>
          <v-chip
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
      colours: ['NONE', 'red', 'orange', 'yellow', 'green', 'blue', 'purple'],
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