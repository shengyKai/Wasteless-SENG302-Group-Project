<template>
  <v-form @submit="register" v-on:submit.prevent>
    <v-container>
      <v-text-field
        v-model="username"
        label="Username"
        outlined
      />

      <v-text-field
        v-model="password"
        label="Password"
        outlined
      />

      <v-text-field
        v-model="confirmPassword"
        label="Confirm Password"
        outlined
      />

      <v-text-field
        v-model="name"
        label="Name"
        outlined
      />

      <v-text-field
        v-model="bio"
        label="Bio"
        outlined
      />

      <v-text-field
        v-model="dob"
        label="Date of Birth"
        outlined
      />

      <v-text-field
        v-model="phone"
        label="Phone"
        outlined
      />

      <v-text-field
        v-model="address"
        label="Address"
        outlined
      />

      <div class="button-row">
        <v-btn @click="showLogin">
          LOGIN
        </v-btn>

        <v-btn type="submit">
          REGISTER
        </v-btn>
      </div>
    </v-container>
  </v-form>
</template>


<script>
export default {
  name: 'Register',
  data() {
    return {
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      bio: '',
      dob: '',
      phone: '',
      address: ''
    }
  },
  methods: {
    showLogin() {
      this.$emit('showLogin');
    },
    register() {
      alert('TODO');
    }
  }
}
</script>


<style scoped>
  .button-row {
    text-align: center;
  }
  .button-row button {
    margin-right: 20px
  }
</style>
