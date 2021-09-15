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
          Nathan Apple LTD
        </router-link>
      </div>
    </template>
    <v-card-text class="subtitle-1">
      {{ eventTitle }}
    </v-card-text>
    <v-card-actions class="justify-center">
      <v-btn class="action-btn white-text" color="primary darken-1">
        <v-icon>mdi-currency-usd</v-icon>
        Buy
      </v-btn>
      <v-btn v-if="event.interested" class="white--text" color="green">
        <v-icon>mdi-thumb-up</v-icon>
        Like 69
      </v-btn>
      <v-btn v-else class="white--text" color="secondary">
        <v-icon class="mr-1">mdi-thumb-down</v-icon>
        Unlike 69
      </v-btn>
      <v-btn class="white--text" color="orange darken-1" @click="fullSaleOpen = true">
        <v-icon>mdi-arrow-top-right-thick</v-icon>
        View Sale
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
    eventTitle() {
      return `You have ${this.interestString} this listing which closes in ${this.daysUntilClose} days`;
    },
    interestString() {
      return this.event.interested? "liked" : "unliked";
    },
    daysUntilClose() {
      const millisecondsPerDay = 24 * 60 * 60 * 1000;
      const today = new Date();
      const closes = new Date(this.event.saleItem.closes);
      return Math.round(Math.abs((closes-today) / millisecondsPerDay));
    },
    businessRoute() {
      //todo
      return "/business/1";
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