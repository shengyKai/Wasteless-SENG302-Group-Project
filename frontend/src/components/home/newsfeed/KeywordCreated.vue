<template>
  <Event :event="event" :title="title">
    <v-card-text>
      {{ creator.firstName }} {{ creator.lastName }} has added "{{ keyword.name }}"
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn color="error" @click="showDeleteDialog=true">Remove</v-btn>
    </v-card-actions>
    <template v-if="showDeleteDialog">
      <DeleteKeyword :keyword="keyword" @closeDialog="showDeleteDialog=false" @keywordDeleted="removeEvent"/>
    </template>
  </Event>
</template>

<script>
import Event from './Event';
import DeleteKeyword from "../../admin/DeleteKeyword.vue";

export default {
  name: 'KeywordCreated',
  data() {
    return {
      showDeleteDialog: false,
    };
  },
  props: {
    event: {
      type: Object,
    },
  },
  components: {
    Event,
    DeleteKeyword,
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
    removeEvent() {
      this.$store.commit('removeEvent', this.event.id);
    }
  }
};
</script>