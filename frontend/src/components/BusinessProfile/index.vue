<template>
  <div>
    <v-row v-if="fromSearch" class="mb-n16 mt-6">
      <v-col class="text-right mt-16 mb-n16">
        <v-btn @click="returnToSearch" color="primary">Return to search</v-btn>
      </v-col>
    </v-row>
    <div v-if='!modifyBusiness' style="margin-top: 100px">
      <v-card>
        <ImageCarousel
          v-if="businessImages"
          :imagesList="businessImages"
          :showControls="permissionToActAsBusiness"
          @change-primary-image="makeImagePrimary"
          ref="businessImageCarousel"
        />
      </v-card>
      <v-card class="body">
        <div class="d-flex flex-column">
          <v-row>
            <v-col cols="6">
              <span><h1>{{ business.name }}</h1></span>
            </v-col>
            <v-col cols="6" class="d-flex justify-end">
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
                  <v-chip class="link-chip link" :color="getAdminColour(admin)" text-color="white"> {{ admin.firstName }} {{ admin.lastName }} </v-chip>
                </router-link>
              </span>
            </v-col>
          </v-row>
          <div v-if='!modifyBusiness'>
            <v-row justify="end">
              <v-col cols="2">
                <v-btn
                  class="white--text"
                  color="secondary"
                  @click="modifyBusiness = true;"
                >
                  <v-icon
                    class="expand-icon"
                    color="white"
                  >
                    mdi-file-document-edit-outline
                  </v-icon>Modify Business
                </v-btn>
              </v-col>
            </v-row>
          </div>
        </v-container>
      </v-card>
    </div>
    <ModifyBusiness
      :business="business"
      v-if="modifyBusiness"
      @discardModifyBusiness="modifyBusiness=false"
    />
  </div>
</template>

<script>
import ModifyBusiness from '@/components/BusinessProfile/ModifyBusiness';
import {getBusiness, makeBusinessImagePrimary} from '../../api/internal';
import convertAddressToReadableText from '@/components/utils/Methods/convertJsonAddressToReadableText';
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules
} from "@/utils";
import ImageCarousel from "@/components/utils/ImageCarousel";
export default {
  name: 'BusinessProfile',
  components: {
    ImageCarousel,
    ModifyBusiness
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
    }
  },

  methods: {
    goSalePage() {
      this.$router.push(`/business/${this.business.id}/listings`);
    },
    async returnToSearch() {
      await this.$router.push({path: '/search/business', query:{...this.$route.query}});
    },
    showAlert() {
      alert("I am the admin");
    },
    getAdminColour(admin) {
      if (admin.id === this.business.primaryAdministratorId) {
        return "red";
      } else {
        return "green";
      }
    },
    changeUpdateCountries() {
      if (this.updateProductCountry) {
        this.updateProductCountry = false;
      } else {
        this.updateProductCountry = true;
      }
    },
    /**
     * Deletes a given image
     * @param imageId ID of the image to delete
     */
    deleteImage(imageId) {
      console.log(imageId);
      //todo
    },
    /**
     * Sets the given image as primary image to be displayed
     * @param imageId ID of the Image to set
     */
    async makeImagePrimary(imageId) {
      this.errorMessage = undefined;
      const result = await makeBusinessImagePrimary(this.business.id, imageId);
      if (typeof result === 'string') {
        this.errorMessage = result;
        this.$refs.businessImageCarousel.forceClose();
      }
    },
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
</style>
