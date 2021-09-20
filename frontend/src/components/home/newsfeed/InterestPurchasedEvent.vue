<template>
  <Event :event="event" title="An item you have liked has been purchased" :error="errorMessage">
    <v-card-text class="pb-1">
      <strong>{{itemBought}}</strong> from <strong><router-link :to="`/business/${business.id}`">{{seller}}</router-link></strong> has been sold.
    </v-card-text>
  </Event>
</template>

<script>
import Event from "@/components/home/newsfeed/Event";

export default {
  name: "InterestPurchasedEvent",
  components: {
    Event
  },
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
  computed: {
    itemBought() {
      return this.event.saleItem.quantity + " " + this.event.saleItem.inventoryItem.product.name;
    },
    boughtSaleItem() {
      return this.event.boughtSaleItem;
    },
    product() {
      return this.boughtSaleItem.product;
    },
    business() {
      return this.product.business;
    },
  },
};
</script>

<style scoped>

</style>