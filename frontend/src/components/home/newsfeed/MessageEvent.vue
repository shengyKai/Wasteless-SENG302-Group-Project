<template>
  <!--<Event :event="event" :title="title">-->
  <div>
    <v-card-text v-bind:style="{ height: computedHeight }">
      New message from: {{userName}}
      Regarding: "{{cardTitle}}" in the {{section}} section of the Marketplace
      {{message}}
    </v-card-text>
    <div class="foot-tools">
      <v-icon v-if="expanded" @click="expand" title="Collapse text">mdi-arrow-up-drop-circle</v-icon>
      <v-icon v-else @click="expand" title="Expand text">mdi-arrow-down-drop-circle</v-icon>
      <v-btn text color="primary" @click="sendReply">reply</v-btn>
    </div>
  </div>
  <!--</Event>-->
</template>

<script>
//import Event from './Event';

export default {
  name: "MessageEvent",
  components: {
    //Event
  },
  props: {
    //event: {
    //  type: Object,
    //  required: true,
    //},
  },
  data() {
    return {
      title: "New message",
      expanded: false,
      computedHeight: '200em'
    };
  },
  methods: {
    expand() {
      this.expanded = !this.expanded;
      if (this.expanded) {
        this.computedHeight = undefined;
      } else {
        this.computedHeight = '200em';
      }
    },
    sendReply() {
      // Open form to write message back
    }
  },
  computed: {
    // Fluff placeholder
    userName() {
      // First name + Last name + ?(Nickname)
      return "Test Name";
    },
    cardTitle() {
      return "Hungry for apples?";
    },
    section() {
      return "For Sale";
    },
    message() {
      return "Voluptas quaerat consequatur esse nihil ipsa sed natus sed. Autem et atque pariatur optio porro. Ex odio " +
          "perferendis voluptas nihil suscipit earum at et. Ab eum corporis deleniti. Repudiandae quaerat totam numquam " +
          "quis iusto tempore. Harum omnis totam ut.  Commodi quibusdam quo provident temporibus. Ut est porro mollitia " +
          "est quidem magnam ex. Modi accusantium beatae quas aut.";
    }
  }
};
</script>

<style scoped>
.foot-tools {
  display: flex;
  flex-wrap: wrap;
}
</style>