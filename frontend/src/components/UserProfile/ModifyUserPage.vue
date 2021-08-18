<template>
  <v-container>
    <v-form v-model="valid">
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
                @keyup="validateAllfield"
                :rules="mandatoryRules.concat(emailRules).concat(maxLongCharRules)"
                outlined
              />

              <!-- INPUT: Password -->
              <v-row>
                <v-col cols="12" sm="6" class="pb-3">
                  <v-text-field
                    ref="password"
                    v-model="user.password"
                    label="New Password"
                    @keyup="validateAllfield"
                    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    :type="showPassword ? 'text' : 'password'"
                    @click:append="showPassword = !showPassword"
                    :rules="passwordNONO"
                    outlined
                  />
                </v-col>

                <!-- INPUT: Confirm Password -->
                <v-col cols="12" sm="6" class="pb-0">
                  <v-text-field
                    ref="confirmPassword"
                    v-model="confirmPassword"
                    @keyup="passwordCheck"
                    label="Confirm New Password"
                    :append-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    @click:append="showConfirmPassword = !showConfirmPassword"
                    :rules="passwordNONO.concat(passwordConfirmationRule)"
                    outlined
                  />
                </v-col>
              </v-row>

              <!-- INPUT: Current Password -->
              <v-text-field
                ref="oldPassword"
                v-model="user.oldPassword"
                label="Current Password"
                :append-icon="showOldPassword ? 'mdi-eye' : 'mdi-eye-off'"
                :type="showOldPassword ? 'text' : 'password'"
                @click:append="showOldPassword = !showOldPassword"
                :rules="currentPassword"
                outlined
              />
            </v-tab-item>
            <!-- TAB: About -->
            <v-tab-item key="about">
              <!-- INPUT: First name -->
              <v-row>
                <v-col cols="12" sm="6" class="pb-0">
                  <v-text-field
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
                <!-- :max="maxDate" -->
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
                    :rules="countryCodeRules.concat(phoneRequiresCountryCodeRule)"
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
              class="ml-4"
              type="submit"
              color="primary"
              :disabled=!valid
              @click="updateProfile"
            >
              Update profile
            </v-btn>
          </v-row>
        </v-card-text>
      </v-card>
    </v-form>
  </v-container>
</template>

<script>
import LocationAutocomplete from '@/components/utils/LocationAutocomplete';

import { getUser } from '@/api/internal';
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
      tab: 'location',
      valid: false,
      user: {
        email: '',
        password: '',
        oldPassword: '',

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
  mounted () {
    //sets maxDate
    this.maxDate = this.minimumDateOfBirth().toISOString().slice(0, 10);
    console.log("Mount");
    console.log(this.user.password);
    console.log(this.user.oldPassword);
    console.log(this.confirmPassword);
    console.log("endMount");
    // if(this.user.password.length === 0
    //     && this.user.oldPassword.length === 0
    //     && this.confirmPassword.length === 0) {
    //   console.log("A");
    //   this.valid === false;
    // }
  },
  methods: {

    validateAllfield() {
      console.log("AA");
      this.$refs.oldPassword.validate(true);
    },
    /**
     * Update Profile after linking up the modify endpoint
     * Next person might have different idea of how/when the updateProfile button will be display
     * Just here to setup everything
    */
    updateProfile() {
      if(this.credentialsCheck()) {
        console.log(JSON.parse(JSON.stringify(this.user)));
      }
      else {
        console.log("NOPE");
      }
    },
    updatePhoneNumber() {
      this.user.phoneNumber = this.countryCode + ' ' + this.phoneDigits;
    },
    /**
     * Check the credentials of user by prompting user to input current password
     * Attempt to perform login to check the credentials
     * With the current store.state.user.email and oldPassword
     * Return TRUE if this.errorMessage === "" as the init data
     * Return False if this.errorMessage contain message
     */
    async credentialsCheck() {
      this.errorMessage = undefined;
      this.errorMessage = await this.$store.dispatch("login", { email : this.$store.state.user.email, password : this.user.oldPassword });
      if(this.errorMessage === "") {
        return true;
      } else {
        return false;
      }
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
     * If account have have business or administered a business, then the minimum year =16
     * Else a normal user account minimum year restriction will be 13
     */
    minimumDateOfBirth () {
      let today = new Date();
      let year = today.getFullYear();
      let month = today.getMonth();
      let day = today.getDate();
      // console.log(this.$store.state.user.businessesAdministered);
      if(this.$store.state.user.businessesAdministered.length >= 1) {
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
        const id = parseInt(this.$route.params.id);
        if (isNaN(id)) return;

        let user;
        if (id === this.$store.state.user.id) {
          user = this.$store.state.user;
        } else {
          user = await getUser(id);
        }
        this.user.firstName = user.firstName ?? '',
        this.user.lastName = user.lastName ?? '',
        this.user.middleName = user.middleName ?? '',
        this.user.nickname = user.nickname ?? '',
        this.user.bio = user.bio ?? '',
        this.user.email = user.email ?? '',
        this.user.dateOfBirth = user.dateOfBirth ?? '',

        this.user.homeAddress = user.homeAddress;
        this.streetAddress = user.homeAddress.streetNumber + ' ' + user.homeAddress.streetName;

        if (user.phoneNumber !== undefined) {
          let parts = user.phoneNumber.split(' ');
          this.countryCode = parts[0];
          this.phoneDigits = parts.slice(1).join(' ');
        }
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

    currentPassword () {
      return [() =>
        ((this.user.password.length === 0 && this.user.email === this.$store.state.user.email) || this.user.oldPassword.length > 0) || 'need current password'
      ];
    },
    /**
     * Validation for new password confirming
     */
    passwordConfirmationRule () {
      return () =>
        this.user.password === this.confirmPassword || 'New passwords and confirm password must match';
    },

    passwordNONO () {
      if(this.user.password.length === 0) return [];
      else return passwordRules;
    },

    phoneRequiresCountryCodeRule () {
      return () =>
        !(this.phoneDigits > 0 && this.countryCode.length < 1) || 'Country code must be present';
    }
  }

};
</script>
