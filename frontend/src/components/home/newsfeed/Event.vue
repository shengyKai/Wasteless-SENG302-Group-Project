<template>
  <div v-if="deleted">
    <v-card-title>
      You notification will be deleted
    </v-card-title>
    <v-card-text>
      Press undo in the next {{ remainingTime }} seconds to cancel deletion of the notification "{{ title }}."
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn color="secondary" ref="undoButton" @click="undoDelete"> Undo </v-btn>
    </v-card-actions>
    <v-card-text class="justify-center">
      <div class="error--text" v-if="errorMessage !== undefined">{{ errorMessage }}</div>
    </v-card-text>
  </div>
  <div v-else>
    <div class="delete">
      <div class="float=right ma-1">
        <v-icon class="deleteButton"
                ref="deleteButton"
                color="red"
                @click.stop="initiateDeletion"
        >
          mdi-trash-can
        </v-icon>
      </div>
      <v-card-title>
        {{ title }}
      </v-card-title>
      <v-card-subtitle>
        {{ date }}, {{ time }}
      </v-card-subtitle>
      <slot/>
      <v-card-text class="justify-center" v-if="errorMessage !== undefined">
        <div class="error--text">{{ errorMessage }}</div>
      </v-card-text>
      <!-- For user to view their option about the available tag to choose from  -->
      <v-row
      >
        <v-col>
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
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-expand-transition>
            <v-card
              v-show="expand"
              max-width="1200"
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
                </v-chip>
              </div>
            </v-card>
          </v-expand-transition>
        </v-col>
      </v-row>
    </div>
  </div>
</template>

<script>
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';
import { formatDate, formatTime } from '@/utils';
import { setEventTag } from "@/api/internal";

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
      errorMessage: undefined,
      deleted: false,
      deletionTime: undefined,
      deleteCardDialog: false,
      editCardDialog: false,

      expand: false,
      colours: ['none', 'red', 'orange', 'yellow', 'green', 'blue', 'purple'],
    };
  },
  computed: {
    /**
     * The time the event was created in a displayable format (hh:mm).
     */
    time() {
      return formatTime(this.event.created);
    },
    /**
     * The date the event was created in a displayable format.
     */
    date() {
      return formatDate(this.event.created);
    },
    /**
     * The time remaining before this event is permenantly deleted.
     */
    remainingTime() {
      if (this.deletionTime) {
        const timeDifference = Math.floor((this.deletionTime - synchronizedTime.now) / 1000) + 10;
        if (timeDifference > 0) {
          return timeDifference;
        }
      }
      return 0;
    }
  },
  methods: {
    /**
     * Temporarily remove a notification from the feed, and set a timeout to delete it permanently
     * if no action is taken within 10 seconds.
     */
    async initiateDeletion() {
      this.$store.commit('stageEventForDeletion', this.event.id);
      this.deleted = true;
      this.deletionTime = synchronizedTime.now;
      setTimeout(() => this.finalizeDeletion(), 10000);
    },
    /**
     * Permanently delete an event if the deletion has not been undone. Restore the event to the feed
     * and display an errorMessage message if the deletion was not successful.
     */
    async finalizeDeletion() {
      if (this.deleted === true) {
        this.$store.dispatch('deleteStagedEvent', this.event.id)
          .then(content => {
            if (typeof content === 'string') {
              this.errorMessage = content;
              this.deleted = false;
            } else {
              this.$store.commit('removeEvent', this.event.id);
            }
          });
      }
    },

    /**
     * Call the setEventTag endpoint when user click on the tag changing button
     * Will render errorMessage message if the response returned with one
     * @param this.event.id To pass the endpoint for the event user wan to update
     * @param colour        Take the colour that user wan to change the tag into
     */
    async tagNotification(colour) {
      const result = await setEventTag(this.event.id, colour);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.expand = false;
      }
    },
    /**
     * Undo the deteletion of a temporarily deleted event and restore it to the user's feed.
     */
    async undoDelete() {
      this.deleted = false;
      this.$store.commit('unstageEventForDeletion', this.event.id);
    },
  }
};
</script>

<style>
.deleteButton {
  float: right;
  margin: 0.25em;
}
.deletion-error {
  color: red;
}
</style>