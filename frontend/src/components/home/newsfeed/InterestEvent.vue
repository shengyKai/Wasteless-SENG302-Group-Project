<template>
  <Event :event="event">
    <template v-slot:title>
      <div>
        {{ event.saleItem.inventoryItem.product.name }}
        <label class="text-subtitle-1"> From </label>
        <router-link
          :to="businessRoute"
          class="text-subtitle-1 grey--text text--darken-2"
        >
          {{ business.name }}
        </router-link>
      </div>
    </template>
    <v-card-text class="subtitle-1">
      {{ eventTitle }}
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn class="pl-3" color="primary darken-1">
        Buy
        <v-icon>mdi-currency-usd</v-icon>
      </v-btn>
      <!-- Thumb up/down button to show and allow user the like & unlike feature -->
      <v-btn v-if="!event.interested" class="ml-2 blue--text" color="grey lighten-2" @click="changeInterest">
        Like
        <v-icon class="ml-1">mdi-thumb-up</v-icon>
      </v-btn>
      <v-btn v-else color="ml-2 grey lighten-2" @click="todo">
        Unlike
        <v-icon class="ml-1">mdi-thumb-down</v-icon>
      </v-btn>
      <!-- A return button for user to go back to business profile-->
      <v-btn class="ml-2 pl-3" color="secondary" @click="fullSaleOpen=true">
        View
        <v-icon class="ml-1">mdi-arrow-top-right-thick</v-icon>
      </v-btn>
    </v-card-actions>
    <v-dialog v-model="fullSaleOpen" max-width="1200" class="white">
      <FullSaleListing :saleItem="event.saleItem"/>
    </v-dialog>
  </Event>
</template>

<script>
import Event from "@/components/home/newsfeed/Event";
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";
export default {
  name: "InterestEvent",
  components: {
    Event,
    FullSaleListing,
  },
  props: {
    event: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fullSaleOpen: false,
    };
  },
  computed: {
    /**
     * Returns the computed title for the notification
     * Title consists of Product name and Business name
     * @returns {string} Title for notification
     */
    eventTitle() {
      return `You have ${this.interestString} this listing which closes in ${this.daysUntilClose} days`;
    },
    business() {
      return this.event.saleItem.inventoryItem.product.business;
    },
    /**
     * String representation of the user's interest in the sale listing
     * @returns {string} Liked or Unliked
     */
    interestString() {
      return this.event.interested? "liked" : "unliked";
    },
    /**
     * The rounded number of days until the sale listing closes
     * @returns {number} Days until sale listing close date
     */
    daysUntilClose() {
      const millisecondsPerDay = 24 * 60 * 60 * 1000;
      const today = new Date();
      const closes = new Date(this.event.saleItem.closes);
      return Math.round(Math.abs((closes-today) / millisecondsPerDay));
    },
    /**
     * Returns the location descriptor object for routing to the business's profile
     * @returns {object} Location descriptor for business profile
     */
    businessRoute() {
      return {
        name: "businessProfile",
        params: {
          id: this.business.id
        }
      };
    }
  },

};
</script>

<style scoped>
.dirty-centre {
  left: 50%;
  transform: translate(-50%, 0%);
}
</style>