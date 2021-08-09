<template>
  <div>
    <v-row v-if="fromSearch" class="mb-n16 mt-6">
      <v-col class="text-right mt-16 mb-n16">
        <v-btn @click="returnToSearch" color="primary">Return to search</v-btn>
      </v-col>
    </v-row>
    <v-card class="body">
      <div class="top-section">
        <div>
          <h1>
            {{ business.name }}
          </h1>
          <p><b>Created:</b> {{ createdMsg }}</p>
          <v-btn outlined color="primary" @click="goSalePage" :value="false">
            Sale listings
          </v-btn>
        </div>
      </div>

      <v-container fluid>
        <v-row>
          <v-col cols="12" sm="6">
            <h4>Address</h4>
            {{ readableAddress }}
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
              <router-link :to="'/profile/' + admin.id">
                <v-chip class="link-chip link" color="primary"> {{ admin.firstName }} {{ admin.lastName }} </v-chip>
              </router-link>
            </span>
          </v-col>
        </v-row>
      </v-container>
    </v-card>

    <v-form v-model="valid">
      <v-card class="business-modify">
        <v-card-title>Modify Business Details</v-card-title>
        <v-card-text>
          <v-col>
            <v-row>
              <v-text-field
                label="New name of the business"
                v-model="newBusinessName"
              />
            </v-row>
            <v-row>
              <v-text-field
                label="New description of the business"
                v-model="newDescription"
              />
            </v-row>
            <v-row>
              <v-select
                label="New business type of the business"
                v-model="newBusinessType"
                :items="businessTypes"
              />
            </v-row>
          </v-col>
          <v-card-title>Address</v-card-title>
          <v-col>
            <v-row>
              <v-text-field
                label="New street address"
                v-model="newStreetAddress"
              />
            </v-row>
            <v-row>
              <v-text-field
                label="New district"
                v-model="newDistrict"
              />
            </v-row>
            <v-row>
              <v-text-field
                label="New city"
                v-model="newCity"
              />
            </v-row>
            <v-row>
              <v-text-field
                label="New region"
                v-model="newRegion"
              />
            </v-row>
            <v-row>
              <v-text-field
                label="New country"
                v-model="newCountry"
              />
            </v-row>
            <v-row>
              <v-text-field
                label="New postcode"
                v-model="newPostcode"
              />
            </v-row>
          </v-col>
          <v-card-title>Administrators</v-card-title>
          <v-col>
            <v-row>
              <v-card-subtitle>Remove admin placeholder</v-card-subtitle>
            </v-row>
          </v-col>
          <v-col>
            <v-row>
              <v-card-subtitle>Add admin placeholder</v-card-subtitle>
            </v-row>
          </v-col>
        </v-card-text>
        <v-card-actions>
          <v-btn
            type="submit"
            color="primary">
            Modify Business
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>
  </div>
</template>

<script>
import { getBusiness } from '../../api/internal';
import convertAddressToReadableText from '@/components/utils/Methods/convertJsonAddressToReadableText';

export default {
  name: 'BusinessProfile',

  data() {
    return {
      /**
       * The business that this profile is for.
       */
      business: {},
      readableAddress: "",
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      newBusinessName: "",
      newDescription: "",
      newBusinessType: "",
      newStreetAddress: "",
      newDistrict: "",
      newCity: "",
      newRegion: "",
      newCountry: "",
      newPostcode: "",
    };
  },
  watch: {
    $route: {
      handler() {
        const id = parseInt(this.$route.params.id);
        if (isNaN(id)) return;

        getBusiness(id).then((value) => {
          if (typeof value === 'string') {
            this.$store.commit('setError', value);
          } else {
            this.business = value;
            this.readableAddress = convertAddressToReadableText(value.address, "full");
          }
        });
      },
      immediate: true,
    }
  },

  computed: {
    createdMsg() {
      if (this.business.created === undefined) return '';

      const now = new Date();
      const createdAt = new Date(this.business.created);
      const parts = createdAt.toDateString().split(' ');

      const diffTime = now - createdAt;
      const diffMonths = Math.floor(diffTime / (1000 * 60 * 60 * 24 * 30));

      return `${parts[2]} ${parts[1]} ${parts[3]} (${diffMonths} months ago)`;
    },

    administrators() {
      return this.business?.administrators || [];
    },

    fromSearch() {
      return this.$route.query.businessType !== undefined
          || this.$route.query.orderBy !== undefined
          || this.$route.query.page !== undefined
          || this.$route.query.reverse !== undefined
          || this.$route.query.searchQuery !== undefined;
    }
  },

  methods: {
    goSalePage() {
      this.$router.push(`/business/${this.business.id}/listings`);
    },
    async returnToSearch() {
      await this.$router.push({path: '/search/business', query:{...this.$route.query}});
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

.business-modify {
  margin-top: 20px;
}
</style>
