<template>
  <Event :event="event" :title="title">
    <v-card-text>
      <strong>
        Regarding: {{event.conversation.card.title}}
      </strong> <br>
      <div
        class="overflow-y-auto d-flex flex-column-reverse pa-1 rounded"
        style="max-height: 400px; width: 100%;"
      >
        <template v-for="message in messages">
          <div :key="message.id"
               class="message"
               :class="{
                 'align-self-end'   : $store.state.user.id === message.senderId,
                 'align-self-start' : $store.state.user.id !== message.senderId,
               }"
          >
            <v-tooltip bottom>
              <template v-slot:activator="{ on }">
                <div
                  v-on="on"
                  class="elevation-2 pa-1 rounded-lg mt-1"
                  :class="{
                    'message-self'  : $store.state.user.id === message.senderId,
                    'message-other' : $store.state.user.id !== message.senderId,
                  }"
                >
                  {{message.content}}
                </div>
              </template>
              {{ formatDate(message.created) }}, {{ formatTime(message.created) }}
            </v-tooltip>
          </div>
        </template>
        <div class="align-self-center">
          <v-btn v-if="!loadedAll" @click="loadMore" rounded depressed color="secondary" small>Load more</v-btn>
        </div>
      </div>
    </v-card-text>
    <v-row class="ma-0">
      <v-col class="pr-0">
        <v-form v-model="directMessageValid">
          <v-textarea
            v-model="directMessageContent"
            outlined
            auto-grow
            :rows="1"
            label="Enter Message"
            :counter="200"
            prepend-inner-icon="mdi-comment"
            :rules="mandatoryRules.concat(maxCharRules())"
          />
        </v-form>
      </v-col>
      <v-col cols="auto">
        <v-btn color="primary" :disabled="!directMessageValid" @click="sendMessage">
          Send
        </v-btn>
      </v-col>
    </v-row>
  </Event>
</template>

<script>
import Event from './Event';
import {mandatoryRules, maxCharRules} from '@/utils';
import {getMessagesInConversation, messageConversation} from "@/api/internal";
import { formatDate, formatTime } from '@/utils';

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
      messages: [],
      mandatoryRules,
      loadedAll: false,
      messageOwnerDialog: false,
      directMessageContent: '',
      directMessageError: undefined,
      directMessageValid: false,
      maxCharRules: () => maxCharRules(200),
    };
  },
  methods: {
    async loadMore() {
      this.errorMessage = undefined;
      let messages = await getMessagesInConversation(this.card.id, this.event.conversation.buyer.id, 1, this.messages.length + 10);

      if (typeof messages === 'string') {
        this.errorMessage = messages;
        return;
      }

      if (this.messages.length === messages.results.length) {
        this.loadedAll = true;
      }

      this.messages = messages.results;
    },
    async sendMessage() {
      this.errorMessage = undefined;
      let response = await messageConversation(this.card.id, this.$store.state.user.id, this.buyer.id, this.directMessageContent);
      if (typeof response === 'string') {
        this.errorMessage = response;
      }
    },
    formatDate,
    formatTime,
  },
  computed: {
    title() {
      if(this.event.message.senderId === this.$store.state.user.id) {
        return "Conversation with " + this.participant.firstName;
      } else {
        return "New message from: " + this.participant.firstName;
      }
    },
    participant() {
      if (this.$store.state.user.id === this.card.creator.id) {
        return this.buyer;
      } else {
        return this.card.creator;
      }
    },
    buyer() {
      return this.event.conversation.buyer;
    },
    card() {
      return this.event.conversation.card;
    },
  },
  watch: {
    'event.message.id': {
      handler() {
        this.messages.unshift(this.event.message);
      },
      immediate: true,
    },
  },
};
</script>

<style scoped>
.message {
  word-wrap: break-word;
  max-width: 100%;
}

.message-self {
  background-color: var(--v-primary-base);
  color: white;
}

.message-other {
  background-color: var(--v-lightGrey-base);
}


</style>