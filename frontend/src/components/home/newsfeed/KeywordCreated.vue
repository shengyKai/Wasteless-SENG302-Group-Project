<template>
  <Event :event="event" :title="title">
    <v-card-text>
      {{ creator.firstName}} {{ creator.lastName }} has added "{{ keyword.name }}"
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn color="error" @click="deleteKeyword">Remove</v-btn>
    </v-card-actions>
  </Event>
</template>

<script>
import { deleteKeyword } from '@/api/internal';
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
    creator() {
      return this.event.creator;
    },
    keyword() {
      return this.event.keyword;
    },
    title() {
      return `Keyword "${this.keyword.name}" has been created`;
    },
  },
  methods: {
    async deleteKeyword(keywordId) {
      console.log("A");
      let response = await deleteKeyword(keywordId);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }
      this.$emit('keyword-removed');
    },
  },
};
</script>