<template>
  <v-container>
    <v-form v-model="valid" class="login">
      <h1>Sign in</h1>
      <v-text-field
          v-model="email"
          type="email"
          label="Email"
          :rules="usernameRules"
      ></v-text-field>
      <v-text-field
          v-model="password"
          type="password"
          label="Password"
          :rules="passwordRules"
      ></v-text-field>
      <v-btn id="login-button" @click="showProfile" color="primary" :disabled="!valid">Log in</v-btn>
      <v-btn id="register-button" @click="showRegister" color="primary">Register</v-btn>
    </v-form>
  </v-container>
</template>

<script>
export default {
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
    }
  },
  methods: {
    showRegister() {
      this.$emit('showRegister');
    },
    showProfile() {
      alert('TODO');
    }
  }
}
</script>

<style scoped>
.button-row {
  text-align: center;
}
#login-button {
  float: left
}
#register-button {
  float: right
}
</style>