<template>
  <Event :event="event" title="Your marketplace card has been deleted">
    <v-card-text>
      {{ text }}
    </v-card-text>
  </Event>
</template>

<script>
import Event from './Event';

export default {
  name: 'DeleteEvent',
  props: {
    event: {
      type: Object
    },
  },
  components: {
    Event,
  },
  computed: {
    text() {
      return `Your marketplace card "${this.event.title}" in the ${this.section} section has been removed from the marketplace`;
    },
    section() {
      if (this.event.section === "ForSale") {
        return "For Sale";
      }
      return this.event.section;
    },
  }
};
</script>