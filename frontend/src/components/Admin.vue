<template>
  <v-container>
    <!-- Red block to show this is the admin dashbaord/ panel -->
    <v-card height="400px" color="grey lighten-3">
      <v-footer v-bind="localAttrs" :padless="padless">
        <v-card height="70px" rounded width="100%" color="primary" class=" text-center font-weight-bold ">
          <v-divider/>

          <v-card-text class="white--text">
            {{ new Date().getFullYear() }} —
            <strong>Admin Dashboard</strong>
          </v-card-text>
        </v-card>
      </v-footer>
      <v-alert
        v-if="error !== undefined"
        type="error"
        dismissible
        @input="error = undefined"
      >
        {{ error }}
      </v-alert>

      <v-container class="grey lighten-2">
        <v-row>
          <v-col v-for="keyword in keywords" :key="keyword.id" cols="" sm="6" md="4" lg="3">
            <KeywordCard :keyword="keyword"/>
          </v-col>
        </v-row>
      </v-container>
    </v-card>
  </v-container>
</template>

<script>
import { searchKeywords } from "../api/internal.ts";
import KeywordCard from "./cards/KeywordCard.vue";

export default {
  data () {
    return {
      keywords: [],
      error: undefined,
      items: [
        { title: "Dashboard", icon: "mdi-view-dashboard" },
        { title: "Gallery", icon: "mdi-image" },
        { title: "Admin Guide", icon: "mdi-help-box" },
      ],
      right: null,
    };
  },
  methods: {
    async setKeywords() {
      this.error = undefined;
      const response = await searchKeywords("");
      if (typeof response === 'string') {
        this.error = response;
      } else {
        this.keywords = response;
        console.log(this.keywords);
      }
    }
  },
  mounted() {
    this.setKeywords();
  },
  components: {
    KeywordCard,
  }
};
</script>

<style scoped></style>
