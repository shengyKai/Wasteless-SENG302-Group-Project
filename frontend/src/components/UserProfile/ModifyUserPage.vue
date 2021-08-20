<template>
  <v-container>
    <v-form v-model="valid" ref="modifyForm">
      <v-card class="pb-2">
        <v-card-title class="primary-text">Modify Profile</v-card-title>
        <v-card-text>
          <v-tabs v-model="tab">
            <v-tab key="login">Login</v-tab>
            <v-tab key="about">About</v-tab>
            <v-tab key="address">Address</v-tab>
          </v-tabs>
          <v-tabs-items v-model="tab" class="pt-4">
            <!-- TAB: Login -->
            <v-tab-item key="login">
              <!-- INPUT: Email -->
              <v-text-field
                class="required"
                v-model="user.email"
                label="Email"
                @keyup="validateCurrentPassword"
                :rules="mandatoryRules.concat(emailRules).concat(maxLongCharRules)"
                outlined
              />

              <!-- INPUT: Password -->
              <v-row>
                <v-col cols="12" sm="6" class="pb-3">
                  <v-text-field
                    ref="password"
                    v-model="user.newPassword"
                    label="New Password"
                    @keyup="validateCurrentPassword"
                    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    :type="showPassword ? 'text' : 'password'"
                    @click:append="showPassword = !showPassword"
                    :rules="newPasswordRule"
                    outlined
                    autocomplete="new-password"
                  />
                </v-col>

                <!-- INPUT: Confirm Password -->
                <v-col cols="12" sm="6" class="pb-0">
                  <v-text-field
                    ref="confirmPassword"
                    v-model="confirmPassword"
                    label="Confirm New Password"
                    :append-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    @click:append="showConfirmPassword = !showConfirmPassword"
                    :rules="newPasswordRule.concat(passwordConfirmationRule)"
                    outlined
                    autocomplete="new-password"
                  />
                </v-col>
              </v-row>

              <!-- INPUT: Current Password -->
              <v-text-field
                ref="oldPassword"
                v-model="user.password"
                label="Current Password"
                :append-icon="showOldPassword ? 'mdi-eye' : 'mdi-eye-off'"
                :type="showOldPassword ? 'text' : 'password'"
                @click:append="showOldPassword = !showOldPassword"
                :rules="currentPasswordRule"
                outlined
              />
            </v-tab-item>
            <!-- TAB: About -->
            <v-tab-item key="about">
              <!-- INPUT: First name -->
              <v-row>
                <v-col cols="12" sm="6" class="pb-0">
                  <v-text-field
                    ref="firstName"
                    class="required"
                    v-model="user.firstName"
                    label="First name"
                    :rules="mandatoryRules.concat(nameRules).concat(maxMediumCharRules)"
                    outlined
                  />
                </v-col>

                <!-- INPUT: Middle name(s) -->
                <v-col cols="12" sm="6" class="pb-0">
                  <v-text-field
                    v-model="user.middleName"
                    label="Middle name(s)"
                    :rules="nameRules.concat(maxMediumCharRules)"
                    outlined
                  />
                </v-col>

                <!-- INPUT: Last name -->
                <v-col cols="12" sm="6" class="pb-3">
                  <v-text-field
                    class="required"
                    v-model="user.lastName"
                    label="Last name"
                    :rules="mandatoryRules.concat(nameRules).concat(maxMediumCharRules)"
                    outlined
                  />
                </v-col>

                <!-- INPUT: Nickname -->
                <v-col cols="12" sm="6" class="pb-3">
                  <v-text-field
                    v-model="user.nickname"
                    label="Nickname"
                    :rules="alphabetRules.concat(maxMediumCharRules)"
                    outlined
                  />
                </v-col>
              </v-row>

              <!-- INPUT: Bio -->
              <v-textarea
                v-model="user.bio"
                label="Bio"
                rows="3"
                :rules="charBioRules.concat(alphabetExtendedMultilineRules)"
                outlined
              />

              <!-- INPUT: Date of Birth -->
              <v-dialog
                ref="dialog"
                :return-value.sync="user.dateOfBirth"
                v-model="showDatePicker"
                width="300px"
                persistent
              >
                <template v-slot:activator="{ on, attrs }">
                  <v-text-field
                    class="required"
                    label="Date of Birth"
                    v-model="user.dateOfBirth"
                    :rules="mandatoryRules"
                    prepend-inner-icon="mdi-calendar"
                    readonly
                    v-bind="attrs"
                    v-on="on"
                    outlined
                  />
                </template>
                <v-date-picker
                  v-model="user.dateOfBirth"
                  :max="maxDate"
                  scrollable
                >
                  <v-spacer/>
                  <v-btn
                    text
                    color="primary"
                    @click="showDatePicker = false"
                  >
                    Cancel
                  </v-btn>
                  <v-btn
                    text
                    color="primary"
                    @click="$refs.dialog.save(user.dateOfBirth)"
                  >
                    OK
                  </v-btn>
                </v-date-picker>
              </v-dialog>

              <v-row>
                <v-col
                  cols="12"
                  sm="4"
                  class="pb-0"
                >
                  <v-text-field
                    ref="countryCode"
                    v-model="countryCode"
                    label="Country Code"
                    :rules="countryCodeRules.concat(phoneRequiresCountryCodeRules)"
                    outlined
                  />
                </v-col>
                <v-col
                  cols="12"
                  sm="8"
                  class="pb-0"
                >
                  <!-- INPUT: Phone -->
                  <v-text-field
                    label="Phone"
                    v-model="phoneDigits"
                    :rules="phoneNumberRules"
                    @keyup="phoneNumberChange"
                    outlined
                  />
                </v-col>

              </v-row>
            </v-tab-item>
            <!-- TAB: Address -->
            <v-tab-item key="address">
              <!-- INPUT: Street -->
              <v-text-field
                class="required"
                v-model="streetAddress"
                label="Street Address"
                :rules="mandatoryRules.concat(streetNumRules)"
                outlined
              />

              <v-row>
                <v-col cols="12" sm="6" class="pb-0">
                  <!-- INPUT: District/Region/Province -->
                  <LocationAutocomplete
                    type="district"
                    v-model="user.homeAddress.district"
                    :rules="maxLongCharRules.concat(alphabetRules)"
                  />
                </v-col>
                <v-col cols="12" sm="6" class="pb-0">
                  <!-- INPUT: City -->
                  <LocationAutocomplete
                    type="city"
                    class="required"
                    v-model="user.homeAddress.city"
                    :rules="mandatoryRules.concat(alphabetRules).concat(maxLongCharRules)"
                  />
                </v-col>
              </v-row>

              <v-row>
                <v-col cols="12" sm="6" class="pt-0">
                  <!-- INPUT: Region -->
                  <LocationAutocomplete
                    type="region"
                    class="required py-0 my-0"
                    v-model="user.homeAddress.region"
                    :rules="mandatoryRules.concat(alphabetRules).concat(maxLongCharRules)"
                  />
                </v-col>

                <v-col cols="12" sm="6" class="pt-0">
                  <!-- INPUT: Country -->
                  <LocationAutocomplete
                    type="country"
                    class="required"
                    v-model="user.homeAddress.country"
                    :rules="mandatoryRules.concat(alphabetRules).concat(maxLongCharRules)"
                  />
                </v-col>
              </v-row>

              <!-- INPUT: Postcode -->
              <v-text-field
                class="required"
                v-model="user.homeAddress.postcode"
                label="Postcode"
                :rules="mandatoryRules.concat(postCodeRules).concat(maxShortCharRules)"
                outlined
              />
            </v-tab-item>
          </v-tabs-items>


          <!-- Update -->
          <v-divider/>
          <v-row class="mt-2 px-1" justify="end">
            <p class="error-text mt-1" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            <v-btn
              ref="updateButton"
              class="ml-4"
              type="submit"
              color="primary"
              :disabled=!valid
              @click.prevent="updateProfile"
            >
              Update profile
            </v-btn>
            <v-btn
              color="secondary"
              class="ml-2"
              @click="$router.push(`/profile/${id}`);"
            > Discard
              <v-icon
                color="white"
              >
                mdi-file-cancel-outline
              </v-icon>
            </v-btn>
          </v-row>
        </v-card-text>
      </v-card>
    </v-form>
  </v-container>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';

import { getUser, modifyUser } from '@/api/internal';
import {
  alphabetExtendedMultilineRules,
  alphabetRules,
  countryCodeRules,
  emailRules,
  mandatoryRules, maxCharRules,
  nameRules,
  passwordRules, phoneNumberRules,
  postCodeRules,
  streetNumRules,
} from "@/utils";

export default {
  name: 'ModifyUserPage',
  components: {
    LocationAutocomplete,
  },
  data() {
    return {
      id: undefined,
      tab: 'location',
      valid: false,
      user: {
        email: '',
        newPassword: '',
        password: '',

        firstName: '',
        middleName: '',
        lastName: '',
        bio: '',
        nickname: '',
        dateOfBirth: '',
        phoneNumber: '',

        homeAddress: {
          streetNumber: '',
          streetName: '',
          district: '',
          city: '',
          region: '',
          country: '',
          postcode: '',
        }
      },
      previousUser: {},
      countryCode: '',
      phoneDigits: '',
      confirmPassword: '',
      streetAddress: '',
      maxDate: '',
      showDatePicker: false,
      showPassword: false,
      showConfirmPassword: false,
      showOldPassword: false,
      errorMessage: undefined,
    };
  },
  async mounted () {
    await this.setUser();
    this.validateAllField();
    this.maxDate = this.minimumDateOfBirth().toISOString().slice(0, 10);

  },
  methods: {
    validateAllField() {
      this.$refs.modifyForm.validate();
    },
    /**
     * Send a request to the backend to update the user using the details entered in this form.
    */
    async updateProfile() {
      this.errorMessage = undefined;
      let modifiedUser = {
        ...this.user
      };
      if (this.user.newPassword === "") modifiedUser.newPassword = undefined;
      if (this.user.password === "") modifiedUser.password = undefined;
      modifyUser(this.id, modifiedUser)
        .then(response => {
          if (typeof response === 'string') {
            this.errorMessage = response;
          } else if (this.id === this.$store.state.user.id) {
            this.updateStoreUser();
          } else {
            this.$router.push(`/profile/${this.id}`);
          }
        });
    },
    /**
     * Setup all the fields of the user associated with this page.
     */
    async setUser() {
      this.id = parseInt(this.$route.params.id);
      if (isNaN(this.id)) return;

      if (this.id === this.$store.state.user.id) {
        this.previousUser = this.$store.state.user;
      } else {
        this.previousUser = await getUser(this.id);
      }
      this.user.firstName = this.previousUser.firstName ?? '',
      this.user.lastName = this.previousUser.lastName ?? '',
      this.user.middleName = this.previousUser.middleName ?? '',
      this.user.nickname = this.previousUser.nickname ?? '',
      this.user.bio = this.previousUser.bio ?? '',
      this.user.email = this.previousUser.email ?? '',
      this.user.dateOfBirth = this.previousUser.dateOfBirth ?? '',

      this.user.homeAddress = this.previousUser.homeAddress;
      this.streetAddress = this.previousUser.homeAddress.streetNumber + ' ' + this.previousUser.homeAddress.streetName;

      if (this.previousUser.phoneNumber !== undefined) {
        let parts = this.previousUser.phoneNumber.split(' ');
        this.countryCode = parts[0];
        this.phoneDigits = parts.slice(1).join(' ');
      }
    },
    /**
     * Set the attributes of the user in active user in the store to those retrieved from the backend using the
     * user id on this page.
     */
    async updateStoreUser() {
      getUser(this.id)
        .then(response => {
          if (typeof response === 'string') {
            this.errorMessage = response;
          } else {
            this.$store.state.user = response;
            this.$router.push(`/profile/${this.id}`);
          }
        });
    },
    updatePhoneNumber() {
      this.user.phoneNumber = this.countryCode + ' ' + this.phoneDigits;
    },
    /**
     * Apply validation rule on the currentPassword field
     */
    validateCurrentPassword() {
      this.$refs.oldPassword.validate(true);
      this.$refs.confirmPassword.validate(true);
    },
    /**
     * Apply validation rule on the confirmPassword field
     */
    passwordCheck () {
      this.$refs.confirmPassword.validate();
    },
    /**
     * Apply validation rule on country code field
     */
    phoneNumberChange () {
      this.$refs.countryCode.validate();
    },
    /**
     * Set the minimum age range in date picker according to the account
     * Only showing differences between user and business account
     * If account have have business or administered a business, then the minimum year = 16
     * Else a normal user account minimum year restriction will be 13
     */
    minimumDateOfBirth () {
      let today = new Date();
      let year = today.getFullYear();
      let month = today.getMonth();
      let day = today.getDate();
      if(this.previousUser.businessesAdministered.length >= 1) {
        return new Date(year - 16, month, day);
      }
      else {
        return new Date(year - 13, month, day);
      }
    },
  },
  watch: {
    streetAddress: {
      handler() {
        const streetParts = this.streetAddress.split(" ");
        this.user.homeAddress.streetNumber = streetParts[0];
        this.user.homeAddress.streetName = streetParts.slice(1).join(" ");
      },
      immediate: true,
    },
    countryCode() { this.updatePhoneNumber(); },
    phoneDigits() { this.updatePhoneNumber(); },
    $route: {
      async handler() {
        await this.setUser();
      },
      immediate: true,
    },
  },
  /**
   * Use all the imported validation rules from utils to be consistent within the web application.
   */
  computed: {
    emailRules: () => emailRules,
    mandatoryRules: () => mandatoryRules,
    passwordRules: () => passwordRules,
    postCodeRules: () => postCodeRules,
    nameRules: () => nameRules,
    maxShortCharRules: () => maxCharRules(16),
    maxMediumCharRules: () => maxCharRules(32),
    maxLongCharRules: () => maxCharRules(100),
    charBioRules: () => maxCharRules(200),
    phoneNumberRules: () => phoneNumberRules,
    countryCodeRules: () => countryCodeRules,
    alphabetRules: () => alphabetRules,
    alphabetExtendedMultilineRules: () => alphabetExtendedMultilineRules,
    streetNumRules: () => streetNumRules,

    /**
     * Validation for currentPassword field
     * Will be applied/triggered when newPassword or email field(s) is modified
     */
    currentPasswordRule () {
      return [
        () => (this.user.newPassword.length === 0 || this.user.password.length > 0) || 'Current password must be entered to change password',
        () => (this.user.email === this.previousUser.email || this.user.password.length > 0) || 'Current password must be entered to change email'
      ];
    },
    /**
     * Validation for new password and confirm password field matching
     */
    passwordConfirmationRule () {
      return () =>
        this.user.newPassword === this.confirmPassword || 'New passwords and confirm password must match';
    },

    /**
     * Validation rules for new password
     * Not applying rules if the field is empty else validate with passwordRules
     */
    newPasswordRule () {
      if(this.user.newPassword.length === 0) return [];
      else return passwordRules;
    },

    /**
     * Validate rules for phone number
     */
    phoneRequiresCountryCodeRules () {
      return [
        () => !(this.phoneDigits.length > 0 && this.countryCode.length < 1) || 'Country code must be present',
        () => !(this.phoneDigits.length < 1 && this.countryCode.length > 0) || 'Cannot enter country code without phone number',
      ];
    }
  }

};
</script>
