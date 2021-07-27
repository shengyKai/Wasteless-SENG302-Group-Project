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
    async deleteKeyword() {

      let response = await deleteKeyword(this.keyword.id);
      if (typeof response === 'string') {
        this.$store.commit('setError', response);
        return;
      }
      for (let event of this.$store.getters.events) {
        console.log(event.type);
        if (event.type === 'CreateKeywordEvent' && event.keyword.id === this.keyword.id) {
          this.$store.commit("removeEvent", event.id);
          break;
        }
      }


      this.$emit('keyword-deleted');
      this.$emit('closeDialog');
    },
  },
};
</script>