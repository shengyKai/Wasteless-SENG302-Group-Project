<template>
  <v-container>
    <v-card width="600px">
      <v-row>
        <v-col cols="8">
          <v-expand-transition>
            <div v-show="!moreInfo">
              <ProductImageCarousel :productImages="product.images" :productId="product.id" />
              <v-card-title>{{ saleItem.quantity + " x " + product.name }}</v-card-title>
              <v-card-subtitle>{{saleItem.price}}</v-card-subtitle>

            </div>
          </v-expand-transition>
          <v-expand-transition>
            <div v-show="moreInfo">
              <v-divider/>
              <v-card-subtitle>
                Product Description
              </v-card-subtitle>
              <v-card-text>
                {{product.description}}
              </v-card-text>
              <div v-if="saleItem.moreInfo.length !== null && saleItem.moreInfo.length > 0">
                <v-card-subtitle>
                  Additional Sale Info
                </v-card-subtitle>
                <v-card-text>
                  {{saleItem.moreInfo}}
                </v-card-text>
              </div>
            </div>
          </v-expand-transition>
        </v-col>
        <v-col cols="4">
          <v-timeline dense style="height: 100%; margin-left: -40%; margin-bottom: 10px">
            <v-timeline-item color="grey" small>
              <div style="margin-left: -25px">
                <strong>Created</strong>
                {{createdFormatted}}
              </div>
            </v-timeline-item>
            <v-timeline-item color="orange" small>
              <div style="margin-left: -25px">
                <strong>Expires</strong>
                {{expiresFormatted}}
              </div>
            </v-timeline-item>
            <v-timeline-item color="red" small>
              <div style="margin-left: -25px">
                <strong>Closes</strong>
                {{closesFormatted}}
              </div>
            </v-timeline-item>
          </v-timeline>
          <v-card-actions>
            <v-btn style="position: absolute; bottom: 10px; right: 10px" color="secondary" @click="moreInfo=!moreInfo">View {{moreInfo? 'More' : 'Less'}}</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
    </v-card>
  </v-container>
</template>

<script>
import ProductImageCarousel from "@/components/utils/ProductImageCarousel";

export default {
  name: "SaleItem",
  components: {ProductImageCarousel},
  data() {
    return {
      moreInfo: false,
    };
  },
  props: {
    saleItem: {
      default() {
        return {
          "id": 57,
          "inventoryItem": {
            "id": 101,
            "product": {
              "id": "WATT-420-BEANS",
              "name": "Watties Baked Beans - 420g can",
              "description": "Baked Beans as they should be.",
              "manufacturer": "Heinz Wattie's Limited",
              "recommendedRetailPrice": 2.2,
              "created": "2021-05-15T05:55:32.808Z",
              "images": [
                {
                  "id": 1234,
                  "filename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg",
                  "thumbnailFilename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg"
                }
              ]
            },
            "quantity": 4,
            "pricePerItem": 6.5,
            "totalPrice": 21.99,
            "manufactured": "2021-05-15",
            "sellBy": "2021-05-15",
            "bestBefore": "2021-05-15",
            "expires": "2021-05-15"
          },
          "quantity": 3,
          "price": 17.99,
          "moreInfo": "Seller may be willing to consider near offers",
          "created": "2021-07-14T11:44:00Z",
          "closes": "2021-07-21T23:59:00Z"
        };
      }
    },
    businessId: {
      default() {
        return 1;
      }
    },
  },
  computed: {
    product() {
      return this.saleItem.inventoryItem.product;
    },
    inventoryItem() {
      return this.saleItem.inventoryItem;
    },
    createdFormatted() {
      let date = new Date(this.saleItem.created);
      return date.toDateString().slice(3);
    },
    expiresFormatted() {
      let date = new Date(this.saleItem.inventoryItem.expires);
      return date.toDateString().slice(3);
    },
    closesFormatted() {
      let date = new Date(this.saleItem.closes);
      return date.toDateString().slice(3);
    }
  }
};
</script>

<style scoped>

</style>