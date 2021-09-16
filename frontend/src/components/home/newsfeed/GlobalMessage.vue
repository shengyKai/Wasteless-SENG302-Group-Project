<template>
  <Event :event="event" title="Global Message">
    <v-card-text class="subtitle-1">
      {{ event.message }}
    </v-card-text>
  </Event>
</template>

<script>
import Event from './Event';

export default {
  name: 'GlobalMessage',
  components: {
    Event,
  },
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
};
</script>