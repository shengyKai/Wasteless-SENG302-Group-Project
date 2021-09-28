<template>
  <Event :event="event" :title="title" :error="errorMessage">
    <v-card-text class="pb-1">
      You have purchased <strong>{{itemBought}}</strong> from
      <strong>
        <router-link
          class="text--secondary"
          ref="businessLink"
          :to="`/business/${event.boughtSaleItem.product.business.id}`"
        >
          {{seller}}
        </router-link>
      </strong>
      for <strong>${{price}}</strong>.<br>
      This can be collected from <strong>{{location}}</strong> or otherwise arranged with the seller.
    </v-card-text>
  </Event>
</template>

<script>
import Event from "@/components/home/newsfeed/Event";

export default {
  name: "PurchasedEvent",
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
    title() {
      return "Purchased " + this.itemBought;
    },
    address() {
      return this.event.boughtSaleItem.product.business.address;
    },
    itemBought() {
      return this.event.boughtSaleItem.quantity + "x " + this.event.boughtSaleItem.product.name;
    },
    seller() {
      return this.event.boughtSaleItem.product.business.name;
    },
    location() {
      let location = "";
      if (this.address.streetNumber && this.address.streetName) {
        location += this.address.streetNumber + " " + this.address.streetName + ", ";
      }
      if (this.address.district) {
        location += this.address.district + ", ";
      }
      if (this.address.city) {
        location += this.address.city + ", ";
      }
      if (this.address.region) {
        location += this.address.region + ", ";
      }
      location += this.address.country;
      return location;
    },
    price() {
      return this.event.boughtSaleItem.price;
    }
  },
};
</script>