<template>
  <v-card class="body">
    <div class="top-section">
      <div>
        <h1>
          {{ business.name }}
        </h1>
        <p><b>Created:</b> {{ createdMsg }}</p>
      </div>
    </div>

    <v-container fluid>
      <v-row>
        <v-col cols="12" sm="6">
          <h4>Address</h4>
          {{ business.address }}
        </v-col>
        <v-col cols="12" sm="6">
          <h4>Category</h4>
          {{ business.businessType }}
        </v-col>

        <v-col cols="12">
          <h4>Description</h4>
          {{ business.description }}
        </v-col>
      </v-row>
    </v-container>

  </v-card>
</template>

<script>
import { getBusiness } from '../api';

export default {
  name: 'BusinessProfile',

  data() {
    return {
      business: {}
    };
  },

  mounted() {
    const id = parseInt(this.$route.params.id);
    if (isNaN(id)) return;

    getBusiness(id).then((value) => {
      if (typeof value === 'string') {
        // TODO Handle error properly
        console.warn(value);
      } else {
        this.business = value;
      }
    });
  },

  computed: {
    createdMsg() {
      if (this.business.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.business.created);
      const parts = createdAt.toDateString().split(' ');

      const diffTime = now - createdAt;
      const diffMonths = Math.ceil(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    }
  }
};
</script>

<style scoped>
.body {
    padding: 16px;
    width: 100%;
    margin-top: 140px;
    /* text-align: center; */
}

.top-section {
  display: flex;
  flex-wrap: wrap;
  /* justify-content: center; */
}
</style>
