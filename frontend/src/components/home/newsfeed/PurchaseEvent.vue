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
      title: "New Purchase",
      address: this.event.saleItem.inventoryItem.product.business.address
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
      let location;
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