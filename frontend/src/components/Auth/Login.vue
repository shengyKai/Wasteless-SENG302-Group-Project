<template>
  <v-container>
    <v-form v-model="valid" lazy-validation>
      <h1>Sign in</h1>
      <v-text-field
        v-model="email"
        validate-on-blur
        type="email"
        label="Email"
        outlined
        :rules="mandatoryRules().concat(emailRules()).concat(maxCharLongRules())"
      />
      <v-text-field
        v-model="password"
        type="password"
        label="Password"
        :rules="mandatoryRules()"
        outlined
      />
    </v-form>

    <!-- Hidden component, only appear when there's an error and show the response from backend -->
    <p class="login-error-text" v-if ="errorMessage !== undefined"> {{errorMessage}} </p>

    <!-- Login Button Direct user into the home page. Invalid credential will trigger a pop up error message -->
    <v-btn @click="login" type="submit" color="primary" :disabled="!valid">
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
import {emailRules, mandatoryRules, maxCharRules} from "@/utils";

export default {
  loggedIn: true,
  name: "Login",
  data() {
    return {
      valid: false,
      errorMessage: undefined,
      email: "",
      password: "",
      emailRules: () => emailRules,
      mandatoryRules: () => mandatoryRules,
      maxCharShortRules: () => maxCharRules(16),
      maxCharLongRules: () => maxCharRules(255),
    };
  },

  methods: {
    // Method to direct into register page, embed event in a link
    showRegister() {
      this.$emit("showRegister");
    },
    /**
     * Method to log in with the provided user credentials and if they are valid then show home page.
     * Otherwise this will show an error message.
     */
    async login() {
      this.errorMessage = undefined;
      this.errorMessage = await this.$store.dispatch("login", { email : this.email, password : this.password });
      if (this.errorMessage === undefined) {
        await this.$router.push("/home");
      }
    },
  },
};


</script>

<style>
.login-error-text {
  color: var(--v-error-base);
  font-size: 140%;
}
</style>