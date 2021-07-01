<template>
  <div>
    <v-card-title>
      Global Message
    </v-card-title>
    <v-card-subtitle>
      {{ date }}
    </v-card-subtitle>
    <v-card-text>
      {{ event.message }}
    </v-card-text>
  </div>
</template>

<script>
import { formatDate } from '@/utils';

export default {
  name: 'GlobalMessage',
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
  computed: {
    date() {
      return formatDate(this.event.created);
    }
  }
};
</script>