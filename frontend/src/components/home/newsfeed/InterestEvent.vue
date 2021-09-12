<template>
  <Event :event="event" :title="eventTitle">
    <v-card-text>
      <h1 class="title">{{ event.saleItem.inventoryItem.product.name }}</h1>
      <label class=""> From </label>
      <router-link :to="businessRoute">Nathan Apple LTD</router-link>
    </v-card-text>
    <v-card-actions class="action-btn-container">
      <v-btn class="action-btn white-text" color="primary darken-1">
        <v-icon>mdi-currency-usd</v-icon>
        Buy
      </v-btn>
      <v-btn v-if="event.interested" class="action-btn white--text" color="green">
        <v-icon>mdi-thumb-up</v-icon>
        Like 69
      </v-btn>
      <v-btn v-else class="action-btn white--text" color="secondary">
        <v-icon>mdi-thumb-down</v-icon>
        Unlike 69
      </v-btn>
      <v-btn class="action-btn white--text" color="purple">
        <v-icon>mdi-arrow-top-right-thick</v-icon>
        View Sale
      </v-btn>
    </v-card-actions>
  </Event>
</template>

<script>
import Event from "@/components/home/newsfeed/Event";
export default {
  name: "InterestEvent",
  components: {Event},
  props: {
    event: {
      type: Object,
      required: true
    }
  },
  computed: {
    eventTitle() {
      return "You " + (this.event.interested? "liked" : "unliked") +
          ` a listing which closes in ${this.daysUntilClose} days`;
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

</style>