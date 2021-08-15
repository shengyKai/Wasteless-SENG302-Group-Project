<template>
  <Event :event="event" :title="title">
    <v-card-text class="d-flex flex-column align-start" style="text-align: left">
      <strong>
        Regarding: "{{cardTitle}}" in the {{section}} section of the Marketplace
      </strong> <br>
      <div class="card-details" v-bind:style="{'-webkit-line-clamp': lines}">{{message}}</div>
    </v-card-text>
    <v-card-actions class="foot-tools">
      <v-icon v-if="expanded" @click="expand" title="Collapse text">mdi-arrow-up-drop-circle</v-icon>
      <v-icon v-else @click="expand" title="Expand text">mdi-arrow-down-drop-circle</v-icon>
      <v-btn text color="primary" @click="sendReply">reply</v-btn>
    </v-card-actions>
  </Event>
</template>

<script>
import Event from './Event';

export default {
  name: "MessageEvent",
  components: {
    Event
  },
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      expanded: false,
      lines: 3
    };
  },
  methods: {
    expand() {
      this.expanded = !this.expanded;
      if (this.expanded) {
        this.lines = undefined;
      } else {
        this.lines = 3;
      }
    },
    sendReply() {
      // Open form to write message back
    }
  },
  computed: {
    // Fluff placeholder
    title() {
      return "New message from: " + this.userName;
    },
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
  position: absolute;
  bottom:3px;
  right:3px;
}
.card-details {
  float: left;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
}
</style>