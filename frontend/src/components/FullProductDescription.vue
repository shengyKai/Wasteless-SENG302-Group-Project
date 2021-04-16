<template>
  <v-dialog v-model="dialog" max-width="37.5rem">
    <template v-slot:activator="{ on, attrs }">
      <a v-bind="attrs" v-on="on">  Read more... </a>
    </template>
    <v-card>
      <v-card-title>
        <h2>Product Description</h2>
      </v-card-title>
      <v-card-text>
        {{ productDescription }}
        <br>
        <a @click="dialog = false">return</a>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "FullProductDescription",
  props: ["productDescription"],
  data() {
    return {
      dialog: false,
    };
  },
};
</script>
