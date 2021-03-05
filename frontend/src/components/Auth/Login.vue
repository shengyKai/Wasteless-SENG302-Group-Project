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
      <v-btn id="login-button" @click="showProfile" color="primary">Log in</v-btn>    <!--- :disabled="!valid" -->
      <!-- Add in if-else check in ^^^ button-->
      <!-- redirect to profile page -->
              <!-- IF: Logged In -->
        <div v-if="loggedIn">
          <ProfilePage 
            fullName="Tim Tam" 
            nickname="Timmy" 
            bio="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
            email="tim.tam@hotmail.com"
            dateOfBirth="1/1/1900"
            phoneNo="0800 83 83 83"
            address="10 Downing Street"
          />
        </div>

        <!-- IF: Logged Out -->
        <div v-else>
          <Auth />
        </div>
        <v-btn
        class="ma-2"
        color="purple"
        dark
      >
        <v-icon dark>
          mdi-minus-circle
        </v-icon>
      </v-btn>  

      <v-btn depressed class="pink white--text" @click="showLogin">
        <v-icon lefet>email</v-icon>
        <span>nav</span>
      </v-btn>
      <v-btn id="register-button" @click="showRegister" color="primary">Register</v-btn>
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
    }
  },

  
  methods: {
    showRegister() {
      this.$emit('showRegister');
    },
    showProfile() { //hereeeeeeeeee
      alert('TODO');
      this.$emit('showProfile');
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