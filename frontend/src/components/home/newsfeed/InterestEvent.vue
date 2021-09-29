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
      {{ eventText }}
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn class="pl-3" color="primary darken-1">
        Buy
        <v-icon>mdi-currency-usd</v-icon>
      </v-btn>
      <!-- Thumb up/down button to show and allow user the like & unlike feature -->
      <v-btn ref="likeButton" class=" pl-2 pr-2 ml-2" color="grey lighten-2" @click="changeInterest">
        {{thumbMessage}} {{interestCount}}
        <v-icon class="ml-1">{{thumbIcon}}</v-icon>
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
import {setListingInterest} from "@/api/sale";
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
      return this.event.interested? "liked" : "unliked";
    },
    /**
     * Compute the thumb icon base on user interest status
     */
    thumbIcon() {
      if (this.interested) {
        return "mdi-thumb-up";
      } else {
        return "mdi-thumb-up-outline";
      }
    },
    /**
     * Compute the message to be render beside the like icon
     */
    thumbMessage() {
      if (this.interested) {
        return "Liked";
      } else {
        return "Like";
      }
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
    /** Change the user interest status on the listing (toggle)
     */
    async changeInterest() {
      const result = await setListingInterest(
        this.event.saleItem.id,
        {userId: this.$store.state.user.id, interested: !this.interested});
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.interested = !this.interested;
        if(this.interested) this.interestCount += 1;
        else this.interestCount -= 1;
      }
    },
  }
};
</script>

<style scoped>
.dirty-centre {
  left: 50%;
  transform: translate(-50%, 0%);
}
</style>