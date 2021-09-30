<template>
  <div>
    <v-row v-if="fromBusinessSearch && !modifyBusiness" class="mt-6 mb-n10">
      <v-col class="text-right">
        <v-btn @click="returnToBusinessSearch" color="primary">Return to search</v-btn>
      </v-col>
    </v-row>
    <v-row v-if="fromSaleSearch && !modifyBusiness" class="mt-6 mb-n10">
      <v-col class="text-right">
        <v-btn @click="returnToSaleSearch" color="primary">Return to search</v-btn>
      </v-col>
    </v-row>
    <div v-if='!modifyBusiness && this.business' class="mt-16">
      <v-card v-if="businessImages && businessImages.length > 0">
        <ImageCarousel
          :imagesList="businessImages"
          :showMakePrimary="false"
          :showDelete="false"
          ref="businessImageCarousel"
        />
      </v-card>
      <v-card class="pa-5 pt-0">
        <div class="d-flex flex-column" no-gutters>
          <v-row>
            <v-col cols="12">
              <v-alert
                class="ma-2 flex-grow-0"
                v-if="errorMessage !== undefined"
                type="error"
                dismissible
                @input="errorMessage = undefined"
              >
                {{ errorMessage }}
              </v-alert>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="11">
              <span>
                <h1 class="d-inline-block">{{ business.name }}</h1>
                <RankIcon v-if="business.rank.name !== 'bronze'" :rankName="business.rank.name"/>
              </span>
            </v-col>
            <v-col class="text-right" v-if='!modifyBusiness && permissionToActAsBusiness'>
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <v-btn
                    ref="settingsButton"
                    icon
                    color="primary"
                    v-bind="attrs"
                    v-on="on"
                    @click="modifyBusiness = true"
                  >
                    <v-icon>mdi-cog</v-icon>
                  </v-btn>
                </template>
                <span>Modify Business Profile</span>
              </v-tooltip>
            </v-col>
          </v-row>
          <p>
            <strong>Created:</strong> {{ createdMsg }}
            <strong class="rank">Rank:</strong> {{ business.rank.name.charAt(0).toUpperCase() + business.rank.name.slice(1) }}
          </p>
        </div>
        <v-btn class="mr-2" outlined color="primary" @click="goSalePage" :value="false" width="150">
          Sale listings
        </v-btn>
        <v-btn v-if="!isAdmin" class="" outlined color="primary" @click="goSaleReports" :value="false" width="150">
          Sale reports
        </v-btn>
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
            <v-col cols="12" sm="6">
              <h4>Description</h4>
              {{ business.description }}
            </v-col>
            <v-col cols="12" sm="6">
              <h4>Points</h4>
              <LevelUp :business="this.business"/>
            </v-col>
            <v-col cols="12">
              <h4>Administrators</h4>
              <span v-for="admin in administrators" :key="admin.id">
                <router-link :to="'/profile/' + admin.id">
                  <v-chip class="mr-1 link" :color="getAdminColour(admin)" text-color="white"> {{ admin.firstName }} {{ admin.lastName }} </v-chip>
                </router-link>
              </span>
            </v-col>
          </v-row>
        </v-container>
      </v-card>
    </div>

    <ModifyBusiness
      :business="business"
      v-if="modifyBusiness"
      @discardModifyBusiness="updateBusiness"
      @modifySuccess="updateBusiness"
    />
  </div>
</template>

<script>
import { USER_ROLES } from "@/utils";
import ModifyBusiness from '@/components/BusinessProfile/ModifyBusiness';
import convertAddressToReadableText from '@/components/utils/Methods/convertAddressToReadableText';
import ImageCarousel from "@/components/image/ImageCarousel";
import {getBusiness} from "@/api/business";
import RankIcon from "@/components/ranks/RankIcon";
import LevelUp from '@/components/BusinessProfile/LevelUp.vue';
export default {
  name: 'BusinessProfile',
  components: {
    ImageCarousel,
    ModifyBusiness,
    RankIcon,
    LevelUp,
  },
  data() {
    return {
      modifyBusiness: false,
      readableAddress: "",
      errorMessage: undefined,
      dialog: true,
      business: undefined,
    };
  },
  watch: {
    $route: {
      handler() {
        this.updateBusiness();
      },
      immediate: true,
    }
  },

  computed: {
    /**
     * Checks to see if the user is a admin of the business
     */
    isAdmin() {
      return this.business.administrators.map(admin => admin.id).includes(this.$store.state.user.id) ||
      [USER_ROLES.DGAA, USER_ROLES.GAA].includes(this.$store.getters.role);
    },
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
    isActingAsCurrentBusiness() {
      const isBusiness =  this.$store.state.activeRole?.type === 'business';
      if (!isBusiness) return false;
      const user = this.$store.state.user;
      return user.businessesAdministered.map(business => business.id).includes(this.business.id);
    },
    permissionToActAsBusiness() {
      const user = this.$store.state.user;
      return this.isActingAsCurrentBusiness ||
          user.role === 'defaultGlobalApplicationAdmin' ||
          user.role === 'globalApplicationAdmin';
    },

    /**
     * Determines whether the user has been routed to the profile from the business search page
     */
    fromBusinessSearch() {
      return this.$route.query.fromPage === "businessSearch";
    },
    /**
     * Determines whether the user has been routed to the profile from the sale listing search page
     */
    fromSaleSearch() {
      return this.$route.query.fromPage === "saleSearch";
    },
    businessImages() {
      return this.business.images;
    },
  },

  methods: {
    /**
     * Reroutes user to this business' sales page
     */
    goSalePage() {
      this.$router.push(`/business/${this.business.id}/listings`);
    },
    /**
     * Returns to the search page, keeping the search parameters
     */
    async returnToBusinessSearch() {
      await this.$router.push({path: '/search/business', query:{...this.$route.query}});
    },
    /**
     * Returns to the search page, keeping the search parameters
     */
    async returnToSaleSearch() {
      await this.$router.push({path: '/search/sales', query:{...this.$route.query}});
    },
    /**
     * Returns the appropriate color of the admin for their chip
     */
    getAdminColour(admin) {
      if (admin.id === this.business.primaryAdministratorId) {
        return "red";
      } else {
        return "green";
      }
    },
    /**
     * Switches whether to update the products' country to the new country
     */
    changeUpdateCountries() {
      this.updateProductCountry = !this.updateProductCountry;
    },
    /**
     * Shows the Sale Reports page
     */
    goSaleReports() {
      this.$router.push(`/salesreport/${this.$store.state.activeRole.id}`);
    },
    /**
     * Updates the business profile page to show the updated details of the business.
     * This method is separated from the $route watcher as it is reused for the ModifyBusiness page on a successful
     * api call, which will update the business profile page to the latest information.
     */
    updateBusiness() {
      this.modifyBusiness = false;
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
  }
};
</script>
