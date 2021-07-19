<template>
  <div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-text>
      {{ text }}
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn color="primary" @click="removeEvent">Close</v-btn>
    </v-card-actions>
  </div>
</template>

<script>
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
    }
  },
  methods: {
    //Removes the event from the store, which in turn deletes this component from the page
    removeEvent() {
      this.$store.commit("removeEvent", this.event.id);
    }
  }
};
</script>