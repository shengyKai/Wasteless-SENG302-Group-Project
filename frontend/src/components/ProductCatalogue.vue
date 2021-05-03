<template>
  <v-card>
    <v-toolbar dark color="primary">
      <v-select
        v-model="orderBy"
        flat
        solo-inverted
        hide-details
        :items="[
          { text: 'Product Code',               value: 'productCode'},
          { text: 'Product Name',               value: 'name'},
          { text: 'Description',                value: 'description'},
          { text: 'Manufacturer',               value: 'manufacturer'},
          { text: 'Recommended Retail Price',   value: 'recommendedRetailPrice'},
          { text: 'Created',                    value: 'created'},
        ]"
        prepend-inner-icon="mdi-sort-variant"
        label="Sort by"
      />
      <v-col class="text-right">
        <v-btn-toggle class="toggle" v-model="reverse" mandatory>
          <v-btn depressed color="primary" :value="false">
            <v-icon>mdi-arrow-up</v-icon>
          </v-btn>
          <v-btn depressed color="primary" :value="true">
            <v-icon>mdi-arrow-down</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-col>
    </v-toolbar>
    
    <!-- <v-pagination
      v-model="currentPage"
      :length="totalPages"
      circle
    /> -->
  </v-card>
</template>

<script>
import { getProducts } from '../api/internal';
//This component requires two other custom components, one to display the product image, one to view more of the product's description
import FullProductDescription from "./utils/FullProductDescription.vue";
import ProductImageCarousel from "./utils/ProductImageCarousel.vue";

export default {
  name: "ProductCatalogue",

  components: {
    FullProductDescription,
    ProductImageCarousel
  },
  data() {
    return {
      products: [],
      /**
       * Current error message string.
       * If undefined then there is no error.
       */
      error: undefined,
      /**
       * Whether to reverse the search order
       */
      reverse: false,
      /**
       * The current search result order
       */
      orderBy: 'productCode',
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,

      //insert the product images here, do note that the first index, at index 0, will be the primary image of the product.
      //The image MUST be labelled with the key "src" in order to be able to show in component
      productImages: [
        //commented out the images below for the moment, so that if you would like to test out the carousel, just uncomment it
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg",
        },
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/sky.jpg",
        },
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/bird.jpg",
        },
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/planet.jpg",
        },
      ],
      //The bottom 8 variables will all be shown as text/numbers in the component.
      // productName: "",
      // productDescription: "",
      // productDateAdded: "",
      // productManufacturer: "",
      // productRRP: null,
      // productCode: "",

      // try uncommenting the bottom variables to test out how it looks.
      productName: "Some Product",
      productDescription:
        "Some super long description Some super long description Some super long description Some super long description",
      productDateAdded: "Some Date Added",
      productManufacturer: "Some Manufacturer",
      productRRP: 100,
      productCode: "Some Code",

      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false,
    };
  },

  async created() {
    let products = await getProducts(10);
    
  }
  // computed: {
  //   /**
  //    * The total number of pages required to show all the users
  //    * May be 0 if there are no results
  //    */
  //   totalPages () {
  //     return Math.ceil(this.totalResults / this.resultsPerPage);
  //   },
  //   /**
  //    * The message displayed at the bottom of the page to show how many results there are
  //    */
  //   resultsMessage() {
  //     if (this.users.length === 0) return 'There are no results to show';

  //     const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
  //     const pageEndIndex = pageStartIndex + this.users.length;
  //     return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
  //   },
  // },
  methods: {
    //if the "Read more..." link if clicked, readMoreActivated becomes true and the FullProductDescription dialog box will open
    activateReadMore() {
      this.readMoreActivated = true;
    },
    //add the form to edit product details here
    editProductDetails() {
      alert("TODO");
    },

    /**
     * This function gets called when the search results need to change, but the search query has not changed.
     * The page index, results per page, order by and reverse variables notify this function.
     */
    async updateNotQuery() {
      console.log("E");
      const value = await getProduct (
        this.$store.state.activeRole.id,
        this.orderBy,
        this.currentPage,
        this.resultsPerPage,
        this.reverse
      );
      if (typeof value === 'string') {
        this.products = [];
        this.error = value;
      } else {
        this.products = value;
        this.error = undefined;
      }
    },
  },

  watch: {

    orderBy() {
      this.updateNotQuery();
      console.log("A");
    },
    reverse() {
      this.updateNotQuery();
      console.log("B");
    },
    currentPage() {
      this.updateNotQuery();
    },
    resultsPerPage() {
      this.updateNotQuery();
    },
  },
};

</script>

<style scoped>
.product-fields {
  padding-top: 0;
}
</style>