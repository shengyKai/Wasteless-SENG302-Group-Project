<template>
  <div>
    <v-card-title>
      {{ title }}
    </v-card-title>
    <v-card-subtitle>
      {{ date }}
    </v-card-subtitle>
    <v-card-text>
      {{ text }}
    </v-card-text>
    <v-expand-transition>
      <v-container v-show="viewCard">
        <v-row justify="center">
          <MarketplaceCard :isExpiryEvent="true" :content="card" style="width: 300px"/>
        </v-row>
      </v-container>
    </v-expand-transition>
    <v-card-actions class="justify-center">
      <v-btn v-if="!delayed" color="primary" @click="delayExpiry">Delay expiry</v-btn>
      <v-btn color="secondary" @click="viewCard=!viewCard">{{ expandMessage }}</v-btn>
    </v-card-actions>
    <v-card-text class="justify-center">
      <div class="error--text" v-if="errorMessage !== undefined">{{ errorMessage }}</div>
    </v-card-text>
  </div>
</template>

<script>
import { formatDate } from '@/utils';
import { extendMarketplaceCardExpiry } from '@/api/internal';
import MarketplaceCard from '@/components/cards/MarketplaceCard';
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';

export default {
  name: 'ExpiryEvent',
  components: { MarketplaceCard },
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      viewCard: false,
      errorMessage: undefined,
      delayed: false,
    };
  },
  computed: {
    /**
     * Access the card associated with this event.
     */
    card() {
      return this.event.card;
    },
    /**
     * The date the event was created in a displayable format.
     */
    date() {
      return formatDate(this.event.created);
    },
    /**
     * The date and time at which the marketplace card expires.
     */
    expiryDate() {
      return new Date(this.card.displayPeriodEnd);
    },
    /**
     * Number of seconds remaining until the marketplace card expires.
     */
    remainingSeconds() {
      return Math.floor((this.expiryDate - synchronizedTime.now) / 1000);
    },
    /**
     * The amount of time remainging before the marketplace card expires in the format <hours>h <minutes>m <seconds>s.
     */
    remaining() {
      let minutes = Math.floor(this.remainingSeconds / 60);
      let seconds = this.remainingSeconds % 60;
      let hours = Math.floor(minutes / 60);
      minutes = minutes % 60;

      return `${hours}h ${minutes}m ${seconds}s`;
    },
    /**
     * Message to be shown on the button which controls whether the card is expanded.
     */
    expandMessage() {
      if (this.viewCard) {
        return 'Hide card';
      }
      return 'View card';
    },
    /**
     * Title of the newsfeed item. Depends on whether the card's expiry has been delayed.
     */
    title() {
      if (this.delayed) {
        return 'You have delayed your card\'s expiry date';
      } else {
        return 'Your marketplace card is about to expire';
      }
    },
    /**
     * Main text of the newsfeed item. Depends on whether the card's expiry has been delayed.
     */
    text() {
      if (this.delayed) {
        return `The expriy date of your card '${this.card.title}' was delayed by two weeks.`;
      } else {
        return `Your card '${this.card.title}' will expire in ${this.remaining}. Do you want to delay the expiry by two weeks?`;
      }
    }
  },
  watch: {
    /**
     * Watches over the remaniningSeconds computed property such that once it hits 0, the store
     * will remove this event from the store events, thus deleting this component
     */
    remainingSeconds: {
      handler () {
        if (this.remainingSeconds < 0) {
          this.$store.commit("removeEvent", this.event.id);
        }
      },
      immediate: true
    }
  },
  /**
   * Call the api function to delay the card's expiry and show feedback based on the response.
   */
  methods: {
    async delayExpiry() {
      this.errorMessage = await extendMarketplaceCardExpiry(this.card.id);
      if (this.errorMessage === undefined) {
        this.delayed = true;
      }
    }
  },
};
</script>