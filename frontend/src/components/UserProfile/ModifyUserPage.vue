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
                    @keyup="passwordChange"
                    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                    :type="showPassword ? 'text' : 'password'"
                    @click:append="showPassword = !showPassword"
                    :rules="passwordRules.concat(maxMediumCharRules)"
                    outlined
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
                    :rules="passwordConfirmationRule.concat(maxMediumCharRules)"
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
                :rules="passwordRules.concat(maxMediumCharRules)"
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
                <v-col cols="12" sm="6" class="py-0">
                  <v-text-field
                    class="required"
                    v-model="user.lastName"
                    label="Last name"
                    :rules="mandatoryRules.concat(nameRules).concat(maxMediumCharRules)"
                    outlined
                  />
                </v-col>

                <!-- INPUT: Nickname -->
                <v-col cols="12" sm="6" class="py-0">
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
          <v-row class="mt-2 px-2" justify="end">
            <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            <v-btn
              class="ml-2"
              type="submit"
              :disabled="!valid"
              color="primary"
              @click.prevent="updateProfile"
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
        oldPassword: '', // Not sure if this is the final field name

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

      showDatePicker: false,

      showPassword: false,
      showConfirmPassword: false,
      showOldPassword: false,

      errorMessage: undefined,
    };
  },
  methods: {
    updateProfile() {
      console.log(JSON.parse(JSON.stringify(this.user)));
    },
    updatePhoneNumber() {
      this.user.phoneNumber = this.countryCode + ' ' + this.phoneDigits;
    },
    passwordChange() {
      // TODO Write implementation
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
  computed: {
    passwordConfirmationRule: () => [],
    phoneRequiresCountryCodeRule: () => [],
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
  },
  //The computed property below is dependent on two user input fields, password and password confirmation.
  //After the user has typed in the password field, the confirmPassword field would check this rule for each
  //change(in this case, each keystroke), and compare it with the password field. If they are not the same,
  //the error message "Passwords must match" will show up at the bottom of the confirmPassword field, until it
  //is the same.
  passwordConfirmationRule () {
    return () =>
      this.password === this.confirmPassword || 'Passwords must match';
  },
  phoneRequiresCountryCodeRule () {
    return () =>
      !(this.phone.length > 0 && this.countryCode.length < 1) || 'Country code must be present';
  }
};
</script>
