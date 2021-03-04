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

      <v-textarea
        v-model="bio"
        label="Bio"
        outlined
      />

      <v-date-picker
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

      <p @click="showLogin">Already have an account? Login.</p>

      <div class="button-row">

        <v-btn type="submit" color="primary">
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
