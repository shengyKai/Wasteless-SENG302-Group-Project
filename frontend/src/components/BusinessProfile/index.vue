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
        <v-col cols="12">
          <h4>Administrators</h4>
          <span v-for="admin in administrators" :key="admin.id">
            <template v-if="typeof admin === 'string'">
              <v-chip color="error" class="link-chip link"> {{ admin }} </v-chip>
            </template>
            <template v-else>
              <router-link :to="'/profile/' + admin.id">
                <v-chip class="link-chip link" color="primary"> {{ admin.firstName }} {{ admin.lastName }} </v-chip>
              </router-link>
            </template>
          </span>
        </v-col>
      </v-row>
    </v-container>

  </v-card>
</template>

<script>
import { getBusiness, getUser } from '../../api';

export default {
  name: 'BusinessProfile',

  data() {
    return {
      /**
       * The business that this profile is for.
       */
      business: {},
      /**
       * The admin users for this business.
       * Also contains strings that represent fetch errors
       */
      administrators: [],
    };
  },

  mounted() {
    const id = parseInt(this.$route.params.id);
    if (isNaN(id)) return;

    getBusiness(id).then((value) => {
      if (typeof value === 'string') {
        this.$store.commit('setError', value);
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
  },

  watch: {
    async business() {
      this.administrators = await Promise.all((this.business.administrators || []).map(id => getUser(id)));
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

.link-chip {
  margin-right: 4px;
}
</style>
