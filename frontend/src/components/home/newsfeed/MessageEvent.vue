<template>
  <Event :event="event" :title="title">
    <v-card-text class="d-flex flex-column align-start" style="text-align: left">
      <strong>
        Regarding: {{event.conversation.card.title}}
      </strong> <br>
      <v-btn @click="loadMore">Load more messages</v-btn>
      <v-btn v-if="messages.length > 1">Hide messages</v-btn>
      <div
        v-for="message in messages"
        :key="message.id"
        class="elevation-2 pa-1 rounded-lg mt-1 message"
        :class="{
          'self-message'  : $store.state.user.id === message.senderId,
          'other-message' : $store.state.user.id !== message.senderId,
        }"
      >
        {{message.content}}
      </div>
    </v-card-text>
    <v-card-actions class="foot-tools">
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
            Send a message to {{participant.firstName}}
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
import {getMessagesInConversation, messageConversation} from "@/api/internal";

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
      queriedMessages: [],
      mandatoryRules,
      messageOwnerDialog: false,
      directMessageContent: '',
      directMessageError: undefined,
      directMessageValid: false,
      maxCharRules: () => maxCharRules(200),
    };
  },
  methods: {
    async loadMore() {
      let messages = await getMessagesInConversation(this.card.id, this.event.conversation.buyer.id, 1, this.queriedMessages.length + 10);
      this.queriedMessages = messages.results;
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
    messages() {
      if (this.queriedMessages.length !== 0) {
        let messages = this.queriedMessages;
        messages.reverse();
        return messages;
      }
      return [this.event.message];
    },
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
  },
  watch: {
    'event.message.id': function() {
      this.queriedMessages = [];
    },
  },
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

.message {
  word-wrap: break-word;
  max-width: 100%;
}

.self-message {
  background-color: var(--v-primary-base);
  align-self: flex-end;
}

.other-message {
  background-color: var(--v-lightGrey-base);
  /* background-color: var(--v-secondary-lighten3); */
  align-self: flex-start;
}


</style>