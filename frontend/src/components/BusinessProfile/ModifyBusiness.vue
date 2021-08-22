<template>
  <div  class="d-flex flex-column" no-gutters>
    <v-row justify="center">
      <v-col cols="10">
        <v-card max-width=1800px>
          <v-form v-model="valid"  @submit.prevent="proceedWithModifyBusiness">
            <v-card class="mt-5 ">
              <v-card-title class="primary-text">Modify Business Details</v-card-title>
              <v-card-text>
                <v-container>
                  <v-row no-gutters>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: Business Name -->
                      <v-text-field
                        dense
                        class="mr-1 required"
                        v-model="businessName"
                        label="Name of business"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetExtendedSingleLineRules())"
                        outlined
                      />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: Business Type -->
                      <v-select
                        dense
                        class="ml-1 required"
                        v-model="businessType"
                        :items="businessTypes"
                        label="Business Type"
                        :rules="mandatoryRules()"
                        outlined
                      />
                    </v-col>
                    <v-col cols="12" sm="12">
                      <!-- INPUT: Description -->
                      <v-textarea
                        dense
                        v-model="description"
                        label="Description"
                        :rules="maxCharDescriptionRules().concat(alphabetExtendedMultilineRules())"
                        rows="3"
                        outlined
                      />
                    </v-col>
                    <v-col cols="12" sm="12">
                      <v-card-title class="primary-text mt-n7">Address</v-card-title>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: Street Address -->
                      <v-text-field
                        class="mr-1 required"
                        v-model="streetAddress"
                        label="Company Street Address"
                        :rules="mandatoryRules().concat(streetRules())"
                        outlined/>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: District -->
                      <LocationAutocomplete
                        type="district"
                        class="ml-1"
                        v-model="district"
                        :rules="maxCharRules().concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: City -->
                      <LocationAutocomplete
                        type="city"
                        class="mr-1 required"
                        v-model="city"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: Region -->
                      <LocationAutocomplete
                        type="region"
                        class="ml-1 required"
                        v-model="region"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: Country -->
                      <LocationAutocomplete
                        type="country"
                        class="mr-1 required"
                        v-model="country"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                      />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <!-- INPUT: Postcode -->
                      <v-text-field
                        class="ml-1 required"
                        v-model="postcode"
                        label="Postcode"
                        :rules="mandatoryRules().concat(maxCharRules()).concat(postcodeRules())"
                        outlined
                      />
                    </v-col>
                    <v-col
                      class="text-right"
                      cols="12"
                      sm="12"
                    >
                      <!-- INPUT: Update Currency -->
                      <v-checkbox
                        v-model="updateProductCountry"
                        class="mt-n5"
                        label="Update catalogue's currency"
                        color="primary"
                        hide-details
                      />
                    </v-col>
                  </v-row>
                  <div v-if="isPrimaryOwner | isSystemAdmin" class="mt-1">
                    <v-card-title>Change Primary Administrator</v-card-title>
                    <v-row>
                      <v-col>
                        <!-- INPUT: Admin -->
                        <span v-for="admin in administrators" :key="admin.id">
                          <v-chip
                            v-if="adminIsPrimary(admin)"
                            class="ma-1 ml-0"
                            color="red"
                            text-color="white"
                          >
                            {{ admin.firstName }} {{ admin.lastName }}
                          </v-chip>
                          <v-chip
                            v-else
                            class="ma-1 ml-0"
                            color="green"
                            text-color="white"
                            @click="changePrimaryOwner(admin)"
                          >
                            {{ admin.firstName }} {{ admin.lastName }}
                          </v-chip>
                        </span>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-alert v-if="showChangeAdminAlert" color="red" type="error" dense text>
                        {{ primaryAdminAlertMsg }}
                      </v-alert>
                    </v-row>
                  </div>
                  <v-card-title class="mt-n3">Image</v-card-title>
                  <v-card v-if="businessImages && businessImages.length > 0">
                    <ImageCarousel
                      :imagesList="businessImages"
                      :showMakePrimary="true"
                      :showDelete="false"
                      @change-primary-image="makeImagePrimary"
                      ref="businessImageCarousel"
                    />
                  </v-card>
                  <!-- INPUT: Image Uploader -->
                  <v-btn
                    class="upload-image"
                    color="primary"
                    outlined
                    @click="showImageUploaderForm=true"
                  >
                    <v-icon
                      class="expand-icon"
                      color="primary"
                    >
                      mdi-upload
                    </v-icon>
                    Upload new image
                  </v-btn>
                  <BusinessImageUploader
                    v-model="imageFile"
                    v-if="showImageUploaderForm"
                    @closeDialog="showImageUploaderForm=false"
                    @uploadImage="addImage"/>
                  <v-card-text v-if="allImageFiles.length > 0"> Images uploaded: {{ imageNames }} </v-card-text>
                  <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
                </v-container>
              </v-card-text>
              <v-card-actions>
                <v-row>
                  <v-col class="text-right">
                    <!-- INPUT: Submit -->
                    <v-btn
                      type="submit"
                      color="primary"
                      @click.prevent="currencyConfirmDialog = true"
                    >
                      Submit
                      <v-icon
                        class="ml-1 mr-1"
                        color="white"
                      >
                        mdi-file-upload-outline
                      </v-icon>
                    </v-btn>
                    <!-- INPUT: Discard -->
                    <v-btn
                      color="secondary"
                      class="ml-2"
                      @click="discardButton"
                    > Discard
                      <v-icon
                        color="white"
                      >
                        mdi-file-cancel-outline
                      </v-icon>
                    </v-btn>
                  </v-col>
                </v-row>
                <v-dialog
                  ref="confirmDialog"
                  v-model="currencyConfirmDialog"
                  max-width="300px"
                >
                  <!-- INPUT: Confirm Dialog -->
                  <v-card>
                    <v-card-title>
                      Are you sure?
                    </v-card-title>
                    <v-card-text>
                      Updating location for catalogue entries will change all of the listed product(s) currency accordingly
                    </v-card-text>
                    <v-card-actions>
                      <v-spacer/>
                      <v-btn
                        color="primary"
                        text
                        @click="proceedWithModifyBusiness()"
                      >
                        Save Change
                      </v-btn>
                      <v-btn
                        color="primary"
                        text
                        @click="currencyConfirmDialog = false"
                      >
                        Cancel
                      </v-btn>
                    </v-card-actions>
                  </v-card>
                </v-dialog>
              </v-card-actions>
            </v-card>
          </v-form>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import BusinessImageUploader from "@/components/utils/BusinessImageUploader";
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules,
  USER_ROLES
} from "@/utils";
import { modifyBusiness, uploadBusinessImage, makeBusinessImagePrimary } from '@/api/internal';
import ImageCarousel from "@/components/utils/ImageCarousel";

export default {
  name: 'ModifyBusiness',
  components: {
    LocationAutocomplete,
    BusinessImageUploader,
    ImageCarousel
  },
  props: {
    business: Object
  },
  data() {
    return {
      currencyConfirmDialog: false,
      serverUrl: process.env.VUE_APP_SERVER_ADD,
      readableAddress: "",
      errorMessage: undefined,
      dialog: true,
      administrators: this.business.administrators,
      businessName: this.business.name,
      description: this.business.description,
      businessType: this.business.businessType,
      streetAddress: this.business.address.streetNumber + " " + this.business.address.streetName,
      district: this.business.address.district,
      city: this.business.address.city,
      region: this.business.address.region,
      country: this.business.address.country,
      postcode: this.business.address.postcode,
      images: this.business.images || [],
      businessTypes: [
        'Accommodation and Food Services',
        'Charitable organisation',
        'Non-profit organisation',
        'Retail Trade',
      ],
      updateProductCountry: false,
      valid: false,
      showImageUploaderForm: false,
      showAlert: false,
      showChangeAdminAlert: false,
      primaryAdminAlertMsg: "",
      primaryAdministratorId: this.business.primaryAdministratorId,
      newOwnerId : this.$store.state.user.id,
      imageFile: undefined,
      allImageFiles: [],
      maxCharRules: () => maxCharRules(100),
      maxCharDescriptionRules: ()=> maxCharRules(200),
      mandatoryRules: ()=> mandatoryRules,
      alphabetExtendedSingleLineRules: ()=> alphabetExtendedSingleLineRules,
      alphabetExtendedMultilineRules: ()=> alphabetExtendedMultilineRules,
      alphabetRules: ()=> alphabetRules,
      streetRules: ()=> streetNumRules,
      postcodeRules: ()=> postCodeRules,
    };
  },
  computed: {
    /**
     * Check if the current user is the admin of the system
     */
    isSystemAdmin() {
      return [USER_ROLES.DGAA, USER_ROLES.GAA].includes(
        this.$store.getters.role
      );
    },
    /**
     * Check if the current user is the primary owner of the business
     */
    isPrimaryOwner() {
      return this.$store.state.user.id === this.business.primaryAdministratorId;
    },
    imageNames() {
      return this.allImageFiles.map((image) => image.name).join(", ");
    },
    businessImages() {
      return this.business.images;
    }
  },
  methods: {
    /**
     * Loop through all admin to identify business primary owner
     * Return TRUE if the current looping chip is primary owner and no other chip is selected by user
     * If other chip is selected, the chip will be displayed as primary owner with message prompted
     */
    adminIsPrimary(admin) {
      if(admin.id === this.primaryAdministratorId && this.newOwnerId === this.$store.state.user.id) return true;
      if(admin.id === this.newOwnerId) return true;
      return false;
    },
    /**
     * Execute the discard modify functionality and render business profile
     */
    discardButton() {
      this.$emit('discardModifyBusiness');
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
    /**
     * Action(s) of modifying a business
     * Get the street number and name from the street address field.
     * Check existence of new selected primary owner, update to new owner or remain unchange
     * Set up the modified fields
     * Calling API
     */
    async proceedWithModifyBusiness() {
      this.errorMessage = undefined;

      const streetParts = this.streetAddress.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");

      let modifiedFields = {
        primaryAdministratorId: this.newOwnerId,
        name: this.businessName,
        description: this.description,
        address: {
          streetNumber: streetNum,
          streetName: streetName,
          district: this.district,
          city: this.city,
          region: this.region,
          country: this.country,
          postcode: this.postcode
        },
        businessType: this.businessType,
        updateProductCountry: this.updateProductCountry
      };
      const result = await modifyBusiness(this.business.id, modifiedFields);

      /**
       * If the result is a string, means it is an error message, of which it will show up on the page.
       * As such, the modify page will still remain.
       * If the result is undefined(the else case) then an event will be emitted to the parent component, BusinessProfile/index.vue
       * and the modifyBusiness attribute there will be false, thus changing the page to the usual business profile page.
       */
      if (typeof result === 'string') {
        this.errorMessage = result;
      }
      for (let image of this.allImageFiles) {
        if (this.errorMessage === undefined) {
          await this.uploadImage(image);
        }
      }
      if (this.errorMessage === undefined) {
        this.$emit("modifySuccess");
      }
    },
    /**
     * When the user clicks on an administrator a popup message will appear stating that they
     * have changed that user to the primary administrator of the business. Additionally, the
     * colour of said user will change to red, whilst the existing primary administrator will
     * change to green. Futhermore, if the original primary owner is clicked this
     * message will not appear.
     */
    changePrimaryOwner(admin) {
      if (admin.id !== this.business.primaryAdministratorId) {
        this.primaryAdminAlertMsg = `Primary admin will be changed to ${admin.firstName} ${admin.lastName}`;
        this.showChangeAdminAlert = true;
      } else {
        this.showChangeAdminAlert = false;
        this.primaryAdminAlertMsg = "";
      }
      this.newOwnerId = admin.id;
    },
    addImage() {
      this.showImageUploaderForm = false;
      this.allImageFiles.push(this.imageFile);
      this.imageFile = undefined;
    },
    async uploadImage(image) {
      const result = await uploadBusinessImage(this.business.id, image);
      if (typeof result === 'string') {
        this.errorMessage = result;
      }
    }
  },
};
</script>

<style scoped>
.body {
    padding: 16px;
    width: 100%;
    margin-top: 140px;
}

.top-section {
  display: flex;
  flex-wrap: wrap;
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

.upload-image {
  margin-top: 25px;
}
</style>