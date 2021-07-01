<template>
  <div>
    <v-card-title>
      {{ card.title }} is about to expire
    </v-card-title>
    <v-card-subtitle>
      {{ date }}
    </v-card-subtitle>
    <v-card-actions>
      <v-btn color="primary">Delay by two weeks</v-btn>
    </v-card-actions>
    <v-card-text>
      It will expire in {{ remaining }}
      <!-- <MarketplaceCard :content="card" style="max-width: 200px"/> -->
    </v-card-text>
  </div>
</template>

<script>
import { formatDate } from '@/utils';
// import MarketplaceCard from '@/components/cards/MarketplaceCard.vue';

export default {
  // components: { MarketplaceCard },
  name: 'ExpiryEvent',
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      now: new Date(),
    };
  },
  created() {
    setInterval(() => this.now = new Date(), 1000);
  },
  computed: {
    card() {
      return this.event.card;
    },
    date() {
      return formatDate(this.event.created);
    },
    expiryDate() {
      return new Date(this.card.displayPeriodEnd);
    },
    remainingSeconds() {
      return Math.floor((this.expiryDate - this.now) / 1000);
    },
    remaining() {
      let minutes = Math.floor(this.remainingSeconds / 60);
      let seconds = this.remainingSeconds % 60;
      let hours = Math.floor(minutes / 60);
      minutes = minutes % 60;

      return `${hours}h ${minutes}m ${seconds}s`;
    }
  }
};
</script>