<template>
  <div>
    <div class="delete">
      <v-btn color="red" @click="removeNotification">
        <v-icon>mdi-delete</v-icon>
        {{ errorMessage }}
      </v-btn>
    </div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-subtitle>
      {{ date }}, {{ time }}
    </v-card-subtitle>
    <slot/>
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
    }
  }
};
</script>

<style>
.delete {
  float: right;
  margin: 0.25em;
}
</style>