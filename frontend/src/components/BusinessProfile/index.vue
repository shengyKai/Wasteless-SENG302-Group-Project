<template>
  <div>
    <v-row v-if="fromSearch && !modifyBusiness" class="mb-n16 mt-6">
      <v-col class="text-right mt-10 mb-n10">
        <v-btn @click="returnToSearch" color="primary">Return to search</v-btn>
      </v-col>
    </v-row>
    <div v-if='!modifyBusiness' style="margin-top: 100px">
      <v-card v-if="businessImages && businessImages.length > 0">
        <ImageCarousel
          :imagesList="businessImages"
          :showMakePrimary="permissionToActAsBusiness"
          :showDelete="false"
          @change-primary-image="false"
          ref="businessImageCarousel"
        />
      </v-card>
      <v-card class="body">
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
                <h1 class="business-name">{{ business.name }}</h1>
                <SilverRank v-if="rank === 'silver'" class="rank"/>
                <GoldRank v-if="rank === 'gold'" class="rank"/>
                <PlatinumRank v-if="rank === 'platinum'" class="rank"/>
              </span>
            </v-col>z
            <v-col class="text-right" v-if='!modifyBusiness && permissionToActAsBusiness'>
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <v-btn
                    ref="settingsButton"
                    icon
                    color="primary"
                    v-bind="attrs"
                    v-on="on"
                    @click="ranklog()"
                  >
                    <!-- @click="modifyBusiness = true" -->
                    <v-icon>mdi-cog</v-icon>
                  </v-btn>
                </template>
                <span>Modify Business Profile</span>
              </v-tooltip>
            </v-col>
          </v-row>
          <p><strong>Created:</strong> {{ createdMsg }}</p>
          <v-btn outlined color="primary" @click="goSalePage" :value="false" width="150">
            Sale listings
          </v-btn>
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
                  <v-chip class="link-chip link" :color="getAdminColour(admin)" text-color="white"> {{ admin.firstName }} {{ admin.lastName }} </v-chip>
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
      @discardModifyBusiness="modifyBusiness=false"
      @modifySuccess="updateBusiness"
    />
  </div>
</template>

<script>
import ModifyBusiness from '@/components/BusinessProfile/ModifyBusiness';
import convertAddressToReadableText from '@/components/utils/Methods/convertAddressToReadableText';
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules
} from "@/utils";
import ImageCarousel from "@/components/utils/ImageCarousel";
import {getBusiness} from "@/api/business";
import SilverRank from "@/components/ranks/SilverRank";
import GoldRank from "@/components/ranks/GoldRank";
import PlatinumRank from "@/components/ranks/PlatinumRank";
export default {
  name: 'BusinessProfile',
  components: {
    ImageCarousel,
    ModifyBusiness,
    SilverRank,
    GoldRank,
    PlatinumRank
  },
  data() {
    return {
      modifyBusiness: false,
      readableAddress: "",
      errorMessage: undefined,
      dialog: true,
      business: '',
      businessName: '',
      description: '',
      businessType: [],
      streetAddress: '',
      district: '',
      city: '',
      region: '',
      country: '',
      postcode: '',
      rank: '',
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      showImageUploaderForm: false,
      valid: false,
      updateProductCountry: true,
      maxCharRules: () => maxCharRules(100),
      maxCharDescriptionRules: ()=> maxCharRules(200),
      mandatoryRules: ()=> mandatoryRules,
      alphabetExtendedSingleLineRules: ()=> alphabetExtendedSingleLineRules,
      alphabetExtendedMultilineRules: ()=> alphabetExtendedMultilineRules,
      alphabetRules: ()=> alphabetRules,
      streetRules: ()=> streetNumRules,
      postcodeRules: ()=> postCodeRules
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

    fromSearch() {
      return this.$route.query.businessType !== undefined
          || this.$route.query.orderBy !== undefined
          || this.$route.query.page !== undefined
          || this.$route.query.reverse !== undefined
          || this.$route.query.searchQuery !== undefined;
    },
    businessImages() {
      return this.business.images;
    },
  },

  mounted() {
    this.setRank();
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
    async returnToSearch() {
      await this.$router.push({path: '/search/business', query:{...this.$route.query}});
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
    /**
     * Sets the variable rank to be the business's rank
     */
    setRank() {
      if (this.business.rank === undefined) {
        //TODO This should be modified or removed, however it is currently used for testing and show other team members
        this.rank = "silver";
      } else {
        this.rank = this.business.rank;
      }
    }
  }
};
</script>

<style scoped>
.body {
    padding: 16px;
}

.top-section {
  display: flex;
  flex-wrap: wrap;
}

.link-chip {
  margin-right: 4px;
}

.business-modify {
  margin-top: 20px;
}

.modify-business-button {
  display: block;
  margin-right: 48%;
}

.expand-icon {
  padding-right: 10px;
}

.business-name {
  display: inline;
}

.rank {
  display: inline;
  height: 40px;
  margin-left: 10px;
}
</style>
