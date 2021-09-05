<template>
  <div>
    <h1>Temp Sale Listing Page Title</h1>
  </div>
</template>

<script>
//import ImageCarousel from "@/components/utils/ImageCarousel";
//import FullProductDescription from "@/components/utils/FullProductDescription";
//import { currencyFromCountry } from "@/api/currency";
import { formatDate, formatPrice } from '@/utils';

export default {
  name: "SaleListingPage",
  components: {
    //FullProductDescription, ImageCarousel
  },
  data() {
    return {
      currency: {
        code: "",
        symbol: ""
      }
    };
  },
  props: {
    saleItem: Object,
    businessId: Number
  },
  computed: {
    /**
     * Easy access to the product information of the sale item
     */
    product() {
      return this.saleItem.InventoryItem.product;
    },
    /**
     * Easy access to the inventory item information of the sale item
     */
    inventoryItem() {
      return this.saleItem.inventoryItem;
    },
    /**
     * Creates a nicely formatted readable string for the sales creation date
     * @returns {string} CreatedDate
     */
    createdFormatted() {
      let date = new Date(this.saleItem.created);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the sales expiry date
     * @returns {string} ExpiryDate
     */
    expiresFormatted() {
      let date = new Date(this.saleItem.inventoryItem.expires);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the sales close date
     * @returns {string} CloseDate
     */
    closesFormatted() {
      let date = new Date(this.saleItem.closes);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted retail price, including the currency
     * @returns {string} RetailPrice
     */
    retailPrice() {
      if (!this.saleItem.price) {
        return "Not set";
      }
      return this.currency.symbol + formatPrice(this.saleItem.price) + " " + this.currency.code;
    },
  }
};
</script>