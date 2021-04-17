<template>
  <v-dialog v-model="dialog">
    <template v-slot:activator="{ on, attrs }">
      <a
        v-bind="attrs"
        v-on="on"
      >
        Read more...
      </a>
    </template>
    <v-card>
      <v-card-title>
        <h2>Description</h2>
      </v-card-title>
      <v-card-text>
        {{ productDescription }}
        <br>
        <v-card-actions class="pa-0">
          <a @click="dialog = false">return</a>
        </v-card-actions>
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
