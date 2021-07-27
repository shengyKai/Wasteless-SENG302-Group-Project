<template>
  <Event :event="event" :title="title">
    <v-card-text>
      {{ creator.firstName }} {{ creator.lastName }} has added "{{ keyword.name }}"
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn color="error" @click="deleteKeyword">Remove</v-btn>
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
    creator() {
      return this.event.creator;
    },
    title() {
      return `Keyword "${this.keyword.name}" has been created`;
    },
  },
  methods: {
    deleteKeyword() {
      alert('TODO');
    },
  },
};
</script>