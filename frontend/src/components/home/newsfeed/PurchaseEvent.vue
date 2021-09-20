<template>
  <Event :event="event" :title="title" :error="errorMessage">
    <v-card-text class="pb-1">
      You have purchased <b>{{itemBought}}</b> from <b><a ref="bla" :href="viewBusiness()">{{seller}}</a></b> for <b>${{price}}</b>.<br>
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
      address: this.event.boughtSaleItem.product.business.address
    };
  },
  computed: {
    title() {
      return "Purchased " + this.itemBought;
    },
    itemBought() {
      return this.event.boughtSaleItem.quantity + " " + this.event.boughtSaleItem.product.name;
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
  methods: {
    viewBusiness() {
      this.$router.push("/business/" + this.event.boughtSaleItem.product.business.id);
    }
  }
};
</script>

<style scoped>

</style>