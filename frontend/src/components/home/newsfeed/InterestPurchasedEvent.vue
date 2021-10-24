<template>
  <Event :event="event" title="An item you have liked has been purchased">
    <v-card-text class="pb-1">
      <strong>{{itemBought}}</strong>
      from
      <strong>
        <router-link
          class="text--secondary"
          :to="`/business/${business.id}`"
        >
          {{business.name}}
        </router-link>
      </strong>
      has been sold to another user and is no longer available for purchase.
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
    /**
     * Text describing the item that was sold
     */
    itemBought() {
      return this.boughtSaleItem.quantity + "x " + this.boughtSaleItem.product.name;
    },
    /**
     * The listing that was bought
     */
    boughtSaleItem() {
      return this.event.boughtSaleItem;
    },
    /**
     * Product that comprised the listing
     */
    product() {
      return this.boughtSaleItem.product;
    },
    /**
     * Business selling the listing
     */
    business() {
      return this.product.business;
    },
  },
};
</script>