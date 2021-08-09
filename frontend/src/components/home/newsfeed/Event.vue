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
      <v-icon class="deleteButton"
              ref="deleteButton"
              color="red"
              @click.stop="initiateDeletion"
      >
        mdi-trash-can
      </v-icon>
      <div class="deletion-error">
        {{ errorMessage }}
      </div>
    </div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-subtitle>
      {{ date }}, {{ time }}
    </v-card-subtitle>
    <slot/>
  </div>
</template>

<script>
import { formatDate } from '@/utils';
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';

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
     * and display an error message if the deletion was not successful.
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