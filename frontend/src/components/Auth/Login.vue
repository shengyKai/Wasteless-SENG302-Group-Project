<template>
  <v-container>
    <v-form @submit.prevent="login" v-model="valid">
      <h1>Sign in</h1>
      <v-text-field
        v-model="email"
        type="email"
        label="Email"
        outlined
        :rules="usernameRules"
      />
      <v-text-field
        v-model="password"
        type="password"
        label="Password"
        outlined
        :rules="passwordRules"
      />

      <!-- Login button if user already has an account. -->
      <p
        class="link"
        @click="showRegister"
      >
        Don't have an account? Register.
      </p>

      <!-- Login -->
      <v-btn
        type="submit"
        color="primary"
        :disabled="!valid"
      >
        LOGIN
      </v-btn>
    </v-form>
  </v-container>
</template>


<script>
export default {

  loggedIn: true,
  name: 'Login',
  data() {
    return {
      valid: false,
      email: '',
      password: '',
      usernameRules: [

        email => !!email || 'Email is required',
        email => !!email || /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(email) || 'E-mail must be valid'
      ],
      passwordRules: [
        password => !!password || 'Password is required'
      ]
    };
  },


  methods: {
    showRegister() {
      this.$emit('showRegister');
    },
    login() {
      this.$store.dispatch('getUser');
      this.$router.push('/profile');
    }
  }
};
</script>
