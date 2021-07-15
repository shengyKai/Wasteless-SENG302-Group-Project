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
import MarketplaceCard from '@/components/cards/MarketplaceCard';

export default {
  name: 'ExpiryEvent',
  components: { MarketplaceCard },
  props: {
    event: {
      type: Object
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
      return "deleted Event";
    }
  },
};
</script>