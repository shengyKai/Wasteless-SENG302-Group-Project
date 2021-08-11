<template>
  <div>
    <div class="delete">
      <v-icon class="deleteButton"
              color="red"
              @click.stop="removeNotification"
      >
        mdi-trash-can
      </v-icon>
      <div class="deletion-error">
        {{ error }}
      </div>
    </div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-subtitle>
      {{ date }}, {{ time }}
    </v-card-subtitle>
    <slot/>
    <!-- For user to view their option about the available tag to choose from  -->
    <v-row
      justify="start"
      style="min-height: 10px;"
    >
      <v-col class="shrink">
        <!-- The persistent chip that shows the tag for the message (default will be no colour) -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on, attrs }">
            <v-chip
              class="ma-2 ml-4 mb-3"
              :color="event.tag"
              @click="expand = !expand"
              label
              text-color="white"
              v-bind="attrs"
              v-on="on"
            >
              <v-icon left>
                mdi-label
              </v-icon>
              Tag
            </v-chip>
          </template>
          <span>Click to view the available tags.</span>
        </v-tooltip>
        <v-expand-transition>
          <v-card
            v-show="expand"
            height="70"
            width="1000"
            class="mx-auto"
          >
            <div class="font-weight-medium">
              <span class="ml-4">
                Change your Tag:
              </span>
            </div>
            <!--  Content that run through a loop of colours which at the same time set the colour of the chip
              Make the code more maintainable as it will be easy to modify colour in future and get the index
              Trigger a tagNotification when the chip is clicked (will take the colour as param)
        -->
            <div class="ml-3">
              <v-chip
                class="ma-1"
                v-for="colour in colours"
                :key=colour
                :color="colour"
                label
                text-color="white"
                @click="tagNotification(colour)"
              >
                <v-icon left>
                  mdi-label
                </v-icon>
                Tag
              </v-chip>
            </div>
          </v-card>
        </v-expand-transition>
      </v-col>
    </v-row>
    <v-tooltip bottom>
      <template v-slot:activator="{on, attrs }">
        <v-icon v-if="!isCardOwner"
                ref="messageButton"
                color="primary"
                @click.stop="messageOwnerDialog = true; directMessageContent=''"
                v-bind="attrs"
                v-on="on"
        >
          mdi-reply
        </v-icon>
      </template>
      Reply to this message
    </v-tooltip>
    <v-dialog ref="messageDialog"
              v-model="messageOwnerDialog"
              max-width="600px">
      <v-card>
        <v-card color='secondary lighten-2'>
          <v-card-title>
            Send a message to {{firstName}}
          </v-card-title>
          <v-card-subtitle>
            Your message will appear on their feed
          </v-card-subtitle>
        </v-card>
        <v-parallax
          dark
          src="https://cdn.vuetifyjs.com/images/backgrounds/vbanner.jpg"
        >
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
                     @click="messageOwnerDialog = false; directMessageError = undefined; directMessageContent=''">
                Cancel
              </v-btn>
            </v-card-actions>
          </v-form>
        </v-parallax>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import { formatDate, maxCharRules, mandatoryRules, } from '@/utils';
import {deleteNotification, messageConversation} from "@/api/internal";
import { setEventTag } from '../../../api/internal';

export default {
  name: 'Event',
  props: {
    title: {
      type: String,
      required: true,
    },
    event: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      deleteCardDialog: false,
      editCardDialog: false,
      messageOwnerDialog: false,
      directMessageContent: '',
      directMessageError: undefined,
      directMessageValid: false,
      mandatoryRules,
      maxCharRules: () => maxCharRules(200),
      firstname: "haha",

      expand: false,
      colours: ['none', 'red', 'orange', 'yellow', 'green', 'blue', 'purple'],
      error: undefined
    };
  },
  computed: {
    /**
     * The time the event was created in a displayable format (hh:mm).
     */
    time() {
      let fullTime = new Date(this.event.created).toTimeString().split(' ')[0];
      return fullTime.split(':').splice(0, 2).join(':');
    },
    /**
     * The date the event was created in a displayable format.
     */
    date() {
      return formatDate(this.event.created);
    },
  },
  methods: {
    async removeNotification() {
      const result = await deleteNotification(this.event.id);
      if (typeof result === 'string') {
        this.error = result;
      } else {
        this.$store.commit('removeEvent', this.event.id);
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

    /**
     * Call the setEventTag endpoint when user click on the tag changing button
     * Will render error message if the response returned with one
     * @param this.event.id To pass the endpoint for the event user wan to update
     * @param colour        Take the colour that user wan to change the tag into
     */
    async tagNotification(colour) {
      const result = await setEventTag(this.event.id, colour);
      if (typeof result === 'string') {
        this.error = result;
      } else {
        this.error = undefined;
        this.expand = false;
      }
    },
    /**
     * This should be a method that allows user to change their current tag colour into the desired colour
     * Currently only alert `changing tag`, functionality will be implemented in another task
     */
  }
};
</script>

<style>
.delete {
  float: right;
  margin: 0.25em;
}
.deleteButton {
  float: right;
  margin: 0.25em;
}
.deletion-error {
  color: red;
}
</style>