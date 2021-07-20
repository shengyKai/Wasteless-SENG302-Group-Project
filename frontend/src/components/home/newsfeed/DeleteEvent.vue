<template>
  <div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-subtitle>
      {{ date }}
    </v-card-subtitle>
    <v-card-text>
      {{ text }}
    </v-card-text>
  </div>
</template>

<script>
import { formatDate } from '@/utils';

export default {
  name: 'DeleteEvent',
  props: {
    event: {
      type: Object
    },
  },
  data() {
    return {
      title: "Your marketplace card has been deleted"
    };
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
    date() {
      return formatDate(this.event.created);
    }
  }
};
</script>