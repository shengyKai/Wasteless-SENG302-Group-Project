<template>
  <Event :event="event" :title="title" :error="errorMessage">
    <v-card-text class="pb-1">
      You have purchased <b>{{itemBought}}</b> from <b><a :href="viewBusiness()">{{seller}}</a></b> for <b>${{price}}</b>.<br>
      This can be collected from <b>{{location}}</b> or otherwise arranged with the seller.
    </v-card-text>
  </Event>
</template>

<script>
import Event from "@/components/home/newsfeed/Event";

export default {
  name: "PurchaseEvent",
  components: {
    Event
  },
  props: {
    event: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      title: "New Marketplace Purchase"
    };
  },
  computed: {
    itemBought() {
      return this.event.saleItem.quantity + " " + this.event.saleItem.inventoryItem.product.name;
    },
    seller() {
      return this.event.saleItem.inventoryItem.product.business.name;
    },
    location() {
      return this.event.saleItem.inventoryItem.product.business.address;
    },
    price() {
      return this.event.saleItem.price;
    }
  },
  methods: {
    viewBusiness() {
      this.$router.push("/business/" + this.event.saleItem.inventoryItem.product.business.id);
    }
  }
};
</script>

<style scoped>

</style>