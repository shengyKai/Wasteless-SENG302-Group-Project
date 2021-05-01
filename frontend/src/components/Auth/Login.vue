<template>
  <v-container>
    <!-- @submit.prevent="login"-->
    <v-form v-model="valid">
      <h1>Sign in</h1>
      <v-text-field
        v-model="email"
        type="email"
        label="Email"
        outlined
        :rules="mandatoryRules.concat(emailRules).concat(maxCharLongRules)"
      />
      <v-text-field
        v-model="password"
        type="password"
        label="Password"
        outlined
        :rules="mandatoryRules.concat(passwordRules).concat(maxCharShortRules)"
      />
      <!-- Hidden component, only appear when theres errror and show the respond from backend -->
      <p class="error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>

      <!-- Login Button Direct user into the home page. Invalid credential will trigger a pop up error message -->
    </v-form>
    <v-btn @click="showHome" type="submit" color="primary" :disabled="!valid">
      LOGIN
    </v-btn>

    <!-- Register Button Allow user to click on this link and get directed to registration form -->
    <p class="link pt-5" @click="showRegister">
      Don't have an account? Register.
    </p>
  </v-container>

</template>

<script>
// import {login} from '../../api';
export default {
  loggedIn: true,
  name: "Login",
  data() {
    return {
      valid: false,
      errorMessage: undefined,
      email: "",
      password: "",
      emailRules: [
        email =>
          /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/.test(email)
          || ''
      ],
      mandatoryRules: [
        /**
         * All fields with the class "required" will go through this ruleset to ensure the field is not empty.
         * If it does not follow the format, turn text field into red
        */
        (field) => !!field || '',
      ],
      passwordRules: [
        field => (field && field.length >= 7 && field.length <= 16) || '',                    //Password must have 7-16 characters
        field => /^(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+)$/.test(field) || ''                //Must have at least one number and one alphabet'
      ],
      maxCharShortRules: [
        (field) => field.length <= 16 || "",                                                  //Reached max character limit: 16
      ],
      maxCharLongRules: [
        (field) => field.length <= 255 || "",                                                 //Reached max character limit: 255
      ],
    };
  },

  methods: {
    // Method to direct into register page, embed event in a link
    showRegister() {
      this.$emit("showRegister");
    },
    /**
     * Method to direct into home page, embed in a button
     * dispatch details from textfield to store plugin before directing
     * store.dispatch need to await in this case else the globalError might be inconsistent
     * router.push need to be await else when user click 2 times in a row it will caught Avoided redundant navigation
     */
    async showHome() {
      this.errorMessage = undefined;
      await this.$store.dispatch("login", { email : this.email, password : this.password });
      this.$router.push("/home");
      this.errorMessage = this.$store.getters.getglobalError;
      this.$store.commit('clearError');

    },
  },
};


</script>

<style>
.error-text {
  color: var(--v-error-base);
  font-size: 140%;
}
</style>


