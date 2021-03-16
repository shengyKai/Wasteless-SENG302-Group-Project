<template>
  <v-container>
    <v-form @submit.prevent="login" v-model="valid">
      <h1>Sign in</h1>
      <v-text-field
        v-model="email"
        type="email"
        label="Email"
        outlined
        :rules="mandatoryRules.concat(emailRules).concat(maxCharRules)"
      />
      <v-text-field
        v-model="password"
        type="password"
        label="Password"
        outlined
        :rules="mandatoryRules.concat(passwordRules).concat(maxCharRules)"
      />

      <!-- Login button if user already has an account. -->
      <p class="link" @click="showRegister">
        Don't have an account? Register.
      </p>

      <!-- Login -->
      <v-btn type="submit" color="primary" :disabled="!valid">
        LOGIN
      </v-btn>
    </v-form>
  </v-container>
</template>

<script>
export default {
  loggedIn: true,
  name: "Login",
  data() {
    return {
      valid: false,
      email: "",
      password: "",
      emailRules: [
        (email) =>
          /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(email) ||
          "E-mail must be valid",
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        (field) => !!field || "Field is required",
      ],
      passwordRules: [
        field => (field && field.length >= 7) || 'Password must have 7+ characters',
        field => /^(?=.*[0-9])(?=.*[a-zA-Z])([a-zA-Z0-9]+)$/.test(field) || 'Must have at least one number and one alphabet'
      ],
      maxCharRules: [
        (field) => field.length <= 100 || "Reached max character limit: 100",
      ],
    };
  },

  methods: {
    showRegister() {
      this.$emit("showRegister");
    },
    login() {
      this.$store.dispatch("getUser");
      this.$router.push("/profile");
    },
  },
};
</script>
