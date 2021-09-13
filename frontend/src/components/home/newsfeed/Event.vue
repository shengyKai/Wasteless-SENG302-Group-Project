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
  <div v-else @click="markEventAsRead">
    <v-row>
      <v-col>
        <v-tooltip top>
          <template v-slot:activator="{ on, attrs }">
            <v-card-title
              v-bind="attrs"
              v-on="on">
              <v-badge
                overlap
                offset-y=8
                offset-x=-2
                :icon="readBadgeIcon"
                :color="readColour"
              >
                {{ title }}
              </v-badge>
            </v-card-title>
          </template>
          <span>{{envelopToolTip}}</span>
        </v-tooltip>
        <v-card-subtitle>
          {{ date }}, {{ time }}
        </v-card-subtitle>
      </v-col>
      <v-col cols="auto" class="mt-2">
        <!-- Star icon will be 'filled' when user starred the notification else it will be outlined,
              toggle status when clicked (normal | starred)-->
        <v-tooltip top>
          <template v-slot:activator="{ on, attrs }">
            <v-icon
              v-if="(event.status !== 'archived')"
              class="mr-2"
              ref="filledStarButton"
              color="yellow"
              v-bind="attrs"
              v-on="on"
              @click="toggleStarStatus()"
            >
              {{starIcon}}
            </v-icon>
          </template>
          <span>{{starTooltip}}</span>
        </v-tooltip>

        <!-- Render a star-outline icon to visually show a difference between starred and normal event
              change status to starred when clicked -->

        <!-- Archive icon to allow user archiving an event
              change status to archived when clicked -->
        <v-tooltip top>
          <template v-slot:activator="{ on, attrs }">
            <v-icon v-if="!(event.status === 'archived')"
                    class="mr-2"
                    ref="archiveButton"
                    color="blue"
                    v-bind="attrs"
                    v-on="on"
                    @click.stop="changeEventStatus('archived')"
            >
              mdi-archive-outline
            </v-icon>
          </template>
          <span>Archive notification</span>
        </v-tooltip>
        <!-- Trash Bin icon, allowing user to delete an event
              have a 10s undo-time before the event actually got deleted -->
        <v-tooltip top>
          <template v-slot:activator="{ on, attrs }">
            <v-icon class="mr-2"
                    ref="deleteButton"
                    color="red"
                    v-bind="attrs"
                    v-on="on"
                    @click.stop="initiateDeletion"
            >
              mdi-trash-can
            </v-icon>
          </template>
          <span>Delete notification</span>
        </v-tooltip>
      </v-col>
    </v-row>
    <slot/>
    <v-card-text class="justify-center py-0" v-if="errorMessage !== undefined">
      <div class="error--text">{{ errorMessage }}</div>
    </v-card-text>
    <!-- For user to view their option about the available tag to choose from  -->
    <v-row>
      <v-col>
        <!-- The persistent chip that shows the tag for the message (default will be no colour) -->
        <v-tooltip bottom>
          <template v-slot:activator="{ on, attrs }">
            <v-chip
              class="ml-4"
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
    <v-row no-gutters>
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
</template>

<script>
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';
import { formatDate, formatTime } from '@/utils';
import { setEventTag } from "@/api/internal";
import {updateEventAsRead} from "@/api/events";
import { updateEventStatus } from '../../../api/events';

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
    error: String,
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
      eventStatus: ['normal, starred, archived'],
    };
  },
  computed: {
    /**
     * Change tooltip message according to the event status/interest
     */
    starTooltip() {
      if(this.event.status === "starred") return 'Unstar notification';
      else return 'Star notification';
    },
    /**
     * Change the star icon according to the event status/interest
     */
    starIcon() {
      if(this.event.status === "starred") return 'mdi-star';
      else return 'mdi-star-outline';
    },
    /**
     * Change the badge icon based on whether the event has been read
     */
    readBadgeIcon() {
      if (!this.event.read) {
        return "mdi-email";
      } else {
        return "mdi-email-open";
      }
    },
    /**
     * Change the tooltip message base on the event isRead status
     */
    envelopToolTip() {
      if (!this.event.read) {
        return "Unread";
      } else {
        return "Read";
      }
    },
    /**
     * Changes the badge colour based on whether the event has been read
     */
    readColour() {
      if (!this.event.read) {
        return "primary";
      } else {
        return "secondary";
      }
    },
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
        this.$store.dispatch('refreshEventFeed');
      }
    },
    /**
     * Undo the deteletion of a temporarily deleted event and restore it to the user's feed.
     */
    async undoDelete() {
      this.deleted = false;
      this.$store.commit('unstageEventForDeletion', this.event.id);
    },
    /**
     * Sends a request to the backend if the event is unread and marks it as read.
     */
    async markEventAsRead() {
      if (!this.event.read) {
        const result = await updateEventAsRead(this.event.id);
        if (typeof result === 'string') {
          this.errorMessage = result;
        } else {
          this.errorMessage = undefined;
          this.$store.dispatch('refreshEventFeed');
        }
      }
    },
    /**
     * Call changeEventStatus method according to the event status
     */
    toggleStarStatus() {
      if(this.event.status === 'normal') {
        this.changeEventStatus('starred');
      } else {
        this.changeEventStatus('normal');
      }
    },
    /**
     * @param Status the status to be updated for the selected event/notification
     */
    async changeEventStatus(status) {
      const result = await updateEventStatus(this.event.id, status);
      if (typeof result === 'string') {
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        let newEvent = this.event;
        newEvent.status = status;
        this.$store.commit('addEvent', newEvent);
      }
    },
  },
  watch: {
    error: {
      handler() {
        this.errorMessage = this.error;
      },
      immediate: true,
    },
  },
};
</script>

<style>
.deletion-error {
  color: red;
}
</style>