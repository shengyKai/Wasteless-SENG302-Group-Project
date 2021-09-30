<template>
  <Event
    :event="event"
    :title="`${event.saleItem.inventoryItem.product.name} from ${business.name}`"
    :error="errorMessage">
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
      {{ eventText }}
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn class="pl-3" color="primary darken-1" @click="purchaseListing">
        Buy
        <v-icon>mdi-currency-usd</v-icon>
      </v-btn>
      <!-- A return button for user to go back to business profile-->
      <v-btn class="ml-2 pl-3" color="secondary" @click="fullSaleOpen=true">
        View
        <v-icon class="ml-1">mdi-arrow-top-right-thick</v-icon>
      </v-btn>
    </v-card-actions>
    <v-dialog v-model="fullSaleOpen" max-width="1200" class="white">
      <FullSaleListing
        :saleItem="event.saleItem"
        @goBack="fullSaleOpen=false"
        @refresh="fullSaleOpen=false"
        @interestUpdate="updateInterest"/>
    </v-dialog>
  </Event>
</template>

<script>
import Event from "@/components/home/newsfeed/Event";
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";
import {purchaseListing} from "@/api/sale";
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
      interestCount: this.event.saleItem.interestCount,
      interested: this.event.interested,
      errorMessage: undefined,
    };
  },
  computed: {
    /**
     * Returns the computed text for the notification
     * Text contains like status and number of days until sale closes
     * @returns {string} Body text for notification
     */
    eventText() {
      return `You have ${this.interestString} this listing which closes in ${this.daysUntilClose} days`;
    },
    /**
     * Returns the business associated with the sale item
     */
    business() {
      return this.event.saleItem.inventoryItem.product.business;
    },
    /**
     * String representation of the user's interest in the sale listing
     * @returns {string} Liked or Unliked
     */
    interestString() {
      return this.interested? "liked" : "unliked";
    },
    /**
     * The rounded number of days until the sale listing closes
     * @returns {number} Days until sale listing close date
     */
    daysUntilClose() {
      const millisecondsPerDay = 24 * 60 * 60 * 1000;
      const today = new Date();
      const closes = new Date(this.event.saleItem.closes);
      return Math.ceil(Math.abs((closes-today) / millisecondsPerDay));
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
    },
  },
  methods: {
    /**
     * Updates the interest status.
     * Used for when the interest changes outside of the component
     * @param interest Interest value
     */
    updateInterest(interest) {
      this.interested = interest;
    },
    /**
     * Purchases the listing related to this InterestEvent
     * Adds a small delay before purchasing to prevent simultaneous read/delete
     */
    async purchaseListing() {
      this.errorMessage = undefined;
      setTimeout(async () => {
        let response = await purchaseListing(this.event.saleItem.id, this.$store.state.user.id);
        if (typeof response === 'string') {
          this.errorMessage = response;
          return;
        }
        await this.$store.dispatch('refreshEventFeed');
      }, 100);
      console.log("WOOO");
    }
  }
};
</script>