<template>
  <v-dialog
    v-model="dialog"
    persistent
  >
    <!-- <v-form v-model="valid">
        <v-card>
          <v-card-title>
            <h4 class="primary--text">Create Business</h4>
          </v-card-title>
          <v-card-text>
            <v-container>
              <v-row>
                <v-col>
                  <v-text-field
                    class="required"
                    v-model="business"
                    label="Name of business"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetExtendedSingleLineRules())"
                    outlined
                  />
                </v-col>
                <v-col>
                  <v-select
                    class="required"
                    v-model="businessType"
                    :items="businessTypes"
                    label="Business Type"
                    :rules="mandatoryRules()"
                    outlined
                  />
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12">
                  <v-textarea
                    v-model="description"
                    label="Description"
                    :rules="maxCharDescriptionRules().concat(alphabetExtendedMultilineRules())"
                    rows="2"
                    outlined
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="streetAddress"
                    label="Company Street Address"
                    :rules="mandatoryRules().concat(streetRules())"
                    outlined/>
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="district"
                    v-model="district"
                    :rules="maxCharRules().concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="city"
                    class="required"
                    v-model="city"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="region"
                    class="required"
                    v-model="region"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <LocationAutocomplete
                    type="country"
                    class="required"
                    v-model="country"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(alphabetRules())"
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    class="required"
                    v-model="postcode"
                    label="Postcode"
                    :rules="mandatoryRules().concat(maxCharRules()).concat(postcodeRules())"
                    outlined
                  />
                </v-col>
              </v-row>
              <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>
            </v-container>
          </v-card-text>
          <v-card-actions>
            <v-spacer/>
            <v-btn
              color="primary"
              text
              @click="closeDialog">
              Close
            </v-btn>
            <v-btn
              type="submit"
              color="primary"
              :disabled="!valid"
              @click.prevent="createBusiness">
              Create
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-form> -->
    <div class="row">
      <div class="col-md-6 mx-auto p-0">
        <div class="card">
          <div class="login-box">
            <div class="login-snip">
              <input
                id="tab-1"
                type="radio"
                name="tab"
                class="sign-in"
                checked
              ><label for="tab-1" class="tab">Create</label>
              <input
                id="tab-2"
                type="radio"
                name="tab"
                class="sign-up"
              ><label for="tab-2" class="tab">Modify</label>
              <!-- Login | Sign In components -->
              <div class="login-space">
                <div class="login">
                  <div class="group">
                    <label for="user" class="label"
                    >Email</label
                    >
                    <input
                      type="text"
                      v-model="loginEmail"
                      class="input"
                      placeholder="Enter your email"
                    >
                  </div>
                  <div class="group">
                    <label for="pass" class="label"
                    >Password</label
                    >
                    <input
                      type="password"
                      v-model="loginPassword"
                      class="input"
                      data-type="password"
                      placeholder="Enter your password"
                    >
                  </div>
                  <div class="group">
                    <input
                      id="check"
                      type="checkbox"
                      class="check"
                      checked
                    >
                    <label for="check"
                    ><span class="icon"/> Keep me
                      Signed in</label
                    >
                  </div>
                  <div class="group">
                    <input
                      type="submit"
                      class="button"
                      value="Sign In"
                      @click="loginOnly"
                    >
                  </div>
                  <div class="hr"/>
                  <div
                    class=" font-weight-bold validation-message-area"
                    v-if="errorFlag"
                    style="color: red;"
                  >
                    {{ error }}
                  </div>
                </div>
                <!-- Register | Sign Up Form  -->
                <div class="sign-up-form">
                  <div class="group">
                    <label for="user" class="label"
                    >First name</label
                    >
                    <input
                      id="registerFirstName"
                      v-model="firstName"
                      type="text"
                      class="input"
                      placeholder="Your First name"
                    >
                  </div>
                  <div class="group">
                    <label for="user" class="label"
                    >Last name</label
                    >
                    <input
                      id="registerLastName"
                      v-model="lastName"
                      type="text"
                      class="input"
                      placeholder="Your Last name"
                    >
                  </div>
                  <div class="group">
                    <label for="pass" class="label"
                    >Password</label
                    >
                    <input
                      id="registerPassword"
                      v-model="password"
                      type="password"
                      class="input"
                      data-type="password"
                      placeholder="Create your password"
                    >
                  </div>
                  <div class="group">
                    <label for="pass" class="label"
                    >Repeat Password</label
                    >
                    <input
                      id="registerConfirmPassword"
                      v-model="confirmPassword"
                      type="password"
                      class="input"
                      data-type="password"
                      placeholder="Repeat your password"
                    >
                  </div>
                  <div class="group">
                    <label for="user" class="label"
                    >Email address</label
                    >
                    <input
                      id="registerEmail"
                      v-model="email"
                      type="text"
                      class="input"
                      placeholder="Enter your email address"
                    >
                  </div>
                  <!-- Register | Sign Up submit btn -->
                  <div class="group">
                    <input
                      type="submit"
                      class="button"
                      value="Sign Up"
                      @click="registerUser"
                    >
                  </div>
                  <!-- File upload component -->
                  <div class="file-drop-area">
                    <span class="choose-file-button"
                    >Choose Files</span
                    >
                    <span class="file-message"
                    >or drag and drop files here</span
                    >
                    <input
                      type="file"
                      ref="fileImage"
                      class="file-input"
                      accept=".jpg,.jpeg,.png,.gif"
                    >
                  </div>
                  <!-- Spacer -->
                  <div class="hr"/>

                  <!-- Show failed validation message -->
                  <div
                    class="validation-message-area font-weight-bold"
                    v-if="validateFlag"
                    style="color: red;"
                  >
                    {{ validateMessage.toUpperCase() }}
                  </div>
                  <div
                    class=" font-weight-bold validation-message-area"
                    v-if="errorFlag"
                    style="color: red;"
                  >
                    {{ error }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </v-dialog>
</template>

<script>
// import LocationAutocomplete from '@/components/utils/LocationAutocomplete';
import {createBusiness} from '@/api/internal';
import {
  alphabetExtendedMultilineRules,
  alphabetExtendedSingleLineRules, alphabetRules,
  mandatoryRules,
  maxCharRules, postCodeRules, streetNumRules
} from "@/utils";

export default {
  name: 'CreateBusiness',
  // components: {
  //   LocationAutocomplete,
  // },
  data() {
    return {
      errorMessage: undefined,
      dialog: true,
      business: '',
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
      valid: false,
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
  methods: {
    async createBusiness() {
      this.errorMessage = undefined;
      /**
       * Get the street number and name from the street address field.
       */
      const streetParts = this.streetAddress.split(" ");
      const streetNum = streetParts[0];
      const streetName = streetParts.slice(1, streetParts.length).join(" ");
      let business = {
        primaryAdministratorId: this.$store.state.user.id,
        name: this.business,
        description: this.description,
        address: {
          streetNumber: streetNum,
          streetName: streetName,
          district: this.district,
          city: this.city,
          region: this.region,
          country: this.country,
          postcode: this.postcode,
        },
        businessType: this.businessType,
      };
      let response = await createBusiness(business);
      if (response === undefined) {
        this.closeDialog();
        this.$router.go();
      } else {
        this.errorMessage = response;
      }
    },
    closeDialog() {
      this.$emit('closeDialog');
    }
  }
};
</script>

<style scoped>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}

.file-drop-area {
    position: relative;
    display: flex;
    align-items: center;
    max-width: 100%;
    padding: 25px;
    background: linear-gradient(to left, #bd3131, #b06ab3);
    border: 1px dashed rgba(255, 255, 255, 0.4);
    border-radius: 5px;
    transition: 0.2s;
}

.choose-file-button {
    flex-shrink: 0;
    background: rgba(2, 112, 112, 0.9);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 3px;
    padding: 8px 15px;
    margin-right: 10px;
    font-size: 12px;
    text-transform: uppercase;
}

.file-message {
    font-size: small;
    font-weight: 300;
    line-height: 1.4;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.file-input {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    widows: 100%;
    cursor: pointer;
    opacity: 0;
}

/* min height ori 670 */
.login-box {
    width: 100%;
    margin: auto;
    max-width: 525px;
    min-height: 930px;
    position: relative;
    background: url(https://images.unsplash.com/photo-1507208773393-40d9fc670acf?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1268&q=80)
        no-repeat center;
    box-shadow: 0 12px 15px 0 rgba(0, 0, 0, 0.24),
        0 17px 50px 0 rgba(0, 0, 0, 0.19);
}

.login-snip {
    width: 100%;
    height: 100%;
    position: absolute;
    padding: 90px 70px 50px 70px;
    background: rgba(0, 77, 77, 0.9);
}

.login-snip .login,
.login-snip .sign-up-form {
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    position: absolute;
    transform: rotateY(180deg);
    backface-visibility: hidden;
    transition: all 0.4s linear;
}

.login-snip .sign-in,
.login-snip .sign-up,
.login-space .group .check {
    display: none;
}

.login-snip .tab,
.login-space .group .label,
.login-space .group .button {
    text-transform: uppercase;
}

.login-snip .tab {
    font-size: 22px;
    margin-right: 15px;
    padding-bottom: 5px;
    margin: 0 15px 10px 0;
    display: inline-block;
    border-bottom: 2px solid transparent;
}

.login-snip .sign-in:checked + .tab,
.login-snip .sign-up:checked + .tab {
    color: #fff;
    border-color: #1161ee;
}

.login-space {
    min-height: 345px;
    position: relative;
    perspective: 1000px;
    transform-style: preserve-3d;
}

.login-space .group {
    margin-bottom: 15px;
}

.login-space .group .label,
.login-space .group .input,
.login-space .group .button {
    width: 100%;
    color: #fff;
    display: block;
}

.login-space .group .input,
.login-space .group .button {
    border: none;
    padding: 15px 20px;
    border-radius: 25px;
    background: rgba(255, 255, 255, 0.1);
}

.login-space .group input[data-type="password"] {
    -text-security: circle;
    -webkit-text-security: circle;
}

.login-space .group .label {
    color: #aaa;
    font-size: 12px;
}

.login-space .group .button {
    background: #1161ee;
}

.login-space .group label .icon {
    width: 15px;
    height: 15px;
    border-radius: 2px;
    position: relative;
    display: inline-block;
    background: rgba(255, 255, 255, 0.1);
}

.login-space .group label .icon:before,
.login-space .group label .icon:after {
    content: "";
    width: 10px;
    height: 2px;
    background: #fff;
    position: absolute;
    transition: all 0.2s ease-in-out 0s;
}

.login-space .group label .icon:before {
    left: 3px;
    width: 5px;
    bottom: 6px;
    transform: scale(0) rotate(0);
}

.login-space .group label .icon:after {
    top: 6px;
    right: 0;
    transform: scale(0) rotate(0);
}

.login-space .group .check:checked + label {
    color: #fff;
}

.login-space .group .check:checked + label .icon {
    background: #1161ee;
}

.login-space .group .check:checked + label .icon:before {
    transform: scale(1) rotate(45deg);
}

.login-space .group .check:checked + label .icon:after {
    transform: scale(1) rotate(-45deg);
}

.login-snip .sign-in:checked + .tab + .sign-up + .tab + .login-space .login {
    transform: rotate(0);
}

.login-snip .sign-up:checked + .tab + .login-space .sign-up-form {
    transform: rotate(0);
}

*,
:after,
:before {
    box-sizing: border-box;
}

.clearfix:after,
.clearfix:before {
    content: "";
    display: table;
}

.clearfix:after {
    clear: both;
    display: block;
}

a {
    color: inherit;
    text-decoration: none;
}

.hr {
    height: 2px;
    margin: 60px 0 20px 0;
    background: rgba(255, 255, 255, 0.2);
}

.foot {
    text-align: center;
}

.card {
    width: 500px;
    left: 100px;
}

::placeholder {
    color: #b3b3b3;
}
</style>