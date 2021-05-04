<template>
  <!-- use the v-dialog to show a pop up for the carousel -->
  <v-dialog v-model="dialog">
    <template v-slot:activator="{ on, attrs }">
      <!-- put an image over a link, such that now the image will be clickable to activate the pop up dialog -->
      <!--  v-bind="attrs" v-on="on" allows the v-dialog to use this link as the activator for the dialog box -->
      <a v-bind="attrs" v-on="on">
        <!-- productImages[0] will be the primary image for the product. -->
        <v-img height="100%" :src="serverUrl + productImages[0].filename"/>
      </a>
    </template>
    <template>
      <v-carousel
        show-arrows-on-hover
        hide-delimiters
      >
        <!-- iterate through each photo in productImages -->
        <v-carousel-item v-for="(item, i) in productImages" :key="i" :src="item.filename" />
      </v-carousel>
    </template>
  </v-dialog>
</template>
<script>
export default {
  name: "ProductImageCarousel",
  //pass in productImages from parent compoenent
  props: ["productImages"],
  data() {
    return {
      serverUrl: process.env.VUE_APP_SERVER_ADD,
      // if dialog is false, the popup does not appear.
      dialog: false
    };
  }
};
</script>