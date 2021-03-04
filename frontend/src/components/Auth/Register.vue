<template>
  <v-form @submit="register" v-on:submit.prevent>
    <h1>Register</h1>

    <v-container>
      <!-- INPUT: Username -->
      <v-text-field
        v-model="username"
        label="Username"
        outlined
      />

      <!-- INPUT: Email -->
      <v-text-field
        v-model="email"
        label="Email"
        outlined
      />

      <!-- INPUT: Password -->
      <v-text-field
        v-model="password"
        label="Password"
        outlined
      />

      <!-- INPUT: Confirm Password -->
      <v-text-field
        v-model="confirmPassword"
        label="Confirm Password"
        outlined
      />

      <!-- INPUT: Name -->
      <v-text-field
        v-model="name"
        label="Name"
        outlined
      />

      <!-- INPUT: Bio -->
      <v-textarea
        v-model="bio"
        label="Bio"
        rows="3"
        outlined
      />

      <!-- INPUT: Date of Birth -->
      <v-dialog
        ref="dialog"
        v-model="modal"
        :return-value.sync="dob"
        width="300px"
        persistent
      >
        <template v-slot:activator="{ on, attrs }">
          <v-text-field
            v-model="dob"
            label="Date of Birth"
            prepend-inner-icon="mdi-calendar"
            readonly
            v-bind="attrs"
            v-on="on"
            outlined
          />
        </template>
        <v-date-picker
          v-model="dob"
          scrollable
        >
          <v-spacer></v-spacer>
          <v-btn
            text
            color="primary"
            @click="closeDatePicker"
          >
            Cancel
          </v-btn>
          <v-btn
            text
            color="primary"
            @click="$refs.dialog.save(dob)"
          >
            OK
          </v-btn>
        </v-date-picker>
      </v-dialog>

      <!-- INPUT: Phone -->
      <v-text-field
        v-model="phone"
        label="Phone"
        outlined
      />

      <!-- INPUT: Address -->
      <v-text-field
        v-model="address"
        label="Address"
        outlined
      />


      <!-- Login button if user already has an account. -->
      <p
        class="link"
        @click="showLogin"
      >
        Already have an account? Login.
      </p>

      <!-- Register -->
      <v-btn type="submit" color="primary">
        REGISTER
      </v-btn>
    </v-container>
  </v-form>
</template>


<script>
export default {
  name: 'Register',
  data() {
    return {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      name: '',
      bio: '',
      dob: new Date().toISOString().substr(0, 10),
      phone: '',
      address: '',

      modal: false
    }
  },
  methods: {
    // Show login screen
    showLogin() {
      this.$emit('showLogin');
    },
    // Complete registration with API
    register() {
      console.log(this);
      alert('TODO');
    },
    // Close the date picker modal
    closeDatePicker() {
      this.modal = false;
    }
  }
}
</script>
