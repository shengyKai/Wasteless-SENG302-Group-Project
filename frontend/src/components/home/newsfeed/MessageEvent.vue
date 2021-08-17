<template>
  <Event :event="event" :title="title">
    <v-card-text class="d-flex flex-column align-start" style="text-align: left">
      <strong>
        Regarding: {{event.conversation.card.title}}
      </strong> <br>
      <div class="card-details" v-bind:style="{'-webkit-line-clamp': lines}">{{event.message.content}}</div>
    </v-card-text>
    <v-card-actions class="foot-tools">
      <v-icon v-if="expanded" @click="expand" title="Collapse text">mdi-arrow-up-drop-circle</v-icon>
      <v-icon v-else @click="expand" title="Expand text">mdi-arrow-down-drop-circle</v-icon>
      <v-tooltip bottom >
        <template v-slot:activator="{on, attrs }">
          <v-icon ref="messageButton"
                  color="primary"
                  @click.stop="messageOwnerDialog = true; directMessageContent=''"
                  v-bind="attrs"
                  v-on="on"
                  class="ml-2"
          >
            mdi-reply
          </v-icon>
        </template>
        Reply to this message
      </v-tooltip>
    </v-card-actions>
    <v-dialog ref="messageDialog"
              v-model="messageOwnerDialog"
              max-width="600px">
      <v-card>
        <!-- The 'TITLE' of the replyMessage component -->
        <v-card color='secondary lighten-2'>
          <v-card-title>
            Send a message to {{userName}}
          </v-card-title>
          <v-card-subtitle>
            Your message will appear on their feed
          </v-card-subtitle>
        </v-card>
        <!-- The Message body input component -->
        <v-form v-model="directMessageValid" ref="directMessageForm">
          <v-card-text>
            <v-textarea
              solo
              outlined
              clearable
              prepend-inner-icon="mdi-comment"
              no-resize
              :counter="200"
              :rules="mandatoryRules.concat(maxCharRules())"
              v-model="directMessageContent"/>
          </v-card-text>
          <!-- Submit and Cancel button for the replyMessage component -->
          <v-card-actions>
            <v-alert v-if="directMessageError !== undefined" color="red" type="error" dense text>
              {{directMessageError}}
            </v-alert>
            <v-spacer/>
            <v-btn color="primary"
                   text
                   :disabled="!directMessageValid"
                   @click="sendMessage">
              Send
            </v-btn>
            <v-btn color="primary"
                   text
                   @click="messageOwnerDialog = false;
                           directMessageError = undefined;
                           directMessageContent=''">
              Cancel
            </v-btn>
          </v-card-actions>
        </v-form>
      </v-card>
    </v-dialog>
  </Event>
</template>

<script>
import Event from './Event';
import {mandatoryRules, maxCharRules} from '@/utils';
import {messageConversation} from "@/api/internal";

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
      lines: 3,
      mandatoryRules,
      messageOwnerDialog: false,
      directMessageContent: '',
      directMessageError: undefined,
      directMessageValid: false,
      maxCharRules: () => maxCharRules(200),
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
    async sendMessage() {
      this.directMessageError = undefined;
      let response = await messageConversation(this.content.id, this.$store.state.user.id, this.$store.state.user.id, this.directMessageContent);
      if (typeof response === 'string') {
        this.directMessageError = response;
      } else {
        this.messageOwnerDialog = false;
      }
    },
  },
  computed: {
    // Fluff placeholder
    title() {
      // If the sender ID !== myId >
      if(this.event.message.senderId === this.$store.state.user.id) {
        return "Conversation with " + this.participant.firstName;
      } else {
        return "New message from: " + this.participant.firstName;
      }
    },
    participant() {
      if (this.$store.state.user.id === this.card.creator.id) {
        return this.event.conversation.buyer;
      } else {
        return this.card.creator;
      }
    },
    card() {
      return this.event.conversation.card;
    },
    userName() {
      // First name + Last name + ?(Nickname)
      return "Test Name";
    },
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