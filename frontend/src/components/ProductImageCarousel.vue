<template>
  <v-dialog v-model="dialog">
    <template v-slot:activator="{ on, attrs }">
      <a v-bind="attrs" v-on="on">
        <v-img height="100%" :src="itemImages[0].src"/>
      </a>
    </template>
    <template>
      <v-carousel
        show-arrows-on-hover
        hide-delimiters
      >
        <v-carousel-item v-for="(item, i) in itemImages" :key="i" :src="item.src" />
      </v-carousel>
    </template>
  </v-dialog>
</template>
<script>
export default {
  name: "ProductImageCarousel",
  props: ["itemImages"],
  data() {
    return {
      dialog: false
    };
  }
};
</script>