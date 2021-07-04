<template>
  <div>
    <v-card-title>
      Your marketplace card is about to expire
    </v-card-title>
    <v-card-subtitle>
      {{ date }}
    </v-card-subtitle>
    <v-card-text>
      Your card '{{ card.title }}' will expire in {{ remaining }}. Do you want to delay the expiry by two weeks?
    </v-card-text>
    <v-expand-transition>
      <v-container v-show="viewCard">
        <v-row justify="center">
          <v-col>
            <MarketplaceCard :content="card" style="max-width: 300px"/>
          </v-col>
        </v-row>
      </v-container>
    </v-expand-transition>
    <v-card-actions class="justify-center">
      <v-btn color="primary">Delay expiry</v-btn>
      <v-btn color="secondary" @click="viewCard=!viewCard">{{ expandMessage }}</v-btn>
    </v-card-actions>
  </div>
</template>

<script>
import { formatDate } from '@/utils';
import MarketplaceCard from '@/components/cards/MarketplaceCard';

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
      now: new Date(),
      viewCard: false,
    };
  },
  created() {
    setInterval(() => this.now = new Date(), 1000);
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
      return Math.floor((this.expiryDate - this.now) / 1000);
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
    }
  }
};
</script>