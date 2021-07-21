<template>
  <Event :event="event" :title="title">
    <v-card-actions>
      <v-btn color="error" @click="deleteKeyword">Delete</v-btn>
    </v-card-actions>
  </Event>
</template>

<script>
import Event from './Event';

export default {
  name: 'KeywordCreated',
  props: {
    event: {
      type: Object,
    },
  },
  components: {
    Event,
  },
  computed: {
    keyword() {
      return this.event.keyword;
    },
    title() {
      return `A keyword "${this.keyword.name}" has been created`;
    },
  },
  methods: {
    deleteKeyword() {
      alert('TODO');
    },
  },
};
</script>