<template>
  <v-form @submit="register" v-model="valid">
    <h1>Register</h1>

    <v-container>
      <!-- INPUT: Email -->
      <v-text-field
        class="required"
        v-model="email"
        label="Email"
        :rules="mandatoryRules.concat(emailRules)"
        outlined
      />

      <!-- INPUT: Password -->
      <v-text-field
        class="required"
        v-model="password"
        label="Password"
        :rules="mandatoryRules"
        outlined
      />

      <!-- INPUT: Confirm Password -->
      <v-text-field
        class="required"
        v-model="confirmPassword"
        label="Confirm Password"
        :rules="mandatoryRules.concat(passwordConfirmationRule)"
        outlined
      />

      <!-- INPUT: Name -->
      <v-text-field
        class="required"
        v-model="name"
        label="Name"
        :rules="mandatoryRules"
        outlined
      />

      <!-- INPUT: Nickname -->
      <v-text-field
          v-model="nickname"
          label="Nickname"
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
            class="required"
            v-model="dob"
            label="Date of Birth"
            :rules="mandatoryRules"
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
        class="required"
        v-model="address"
        label="Address"
        :rules="mandatoryRules"
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
      <v-btn
          type="submit"
          color="primary"
          :disabled="!valid">
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
      valid: false,
      email: '',
      password: '',
      confirmPassword: '',
      name: '',
      nickname: '',
      bio: '',
      dob: new Date().toISOString().substr(0, 10),
      phone: '',
      address: '',
      emailRules: [
        email => /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(email) || 'E-mail must be valid'
      ],
      mandatoryRules: [
        field =>  !!field || 'Field is required'
      ],

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
      alert("TODO");
    },
    // Close the date picker modal
    closeDatePicker() {
      this.modal = false;
    }
  },
  computed: {
    //The computed property below is dependent on two user input fields, password and password confirmation.
    //Upon every change on
    passwordConfirmationRule() {
      return () =>
          this.password === this.confirmPassword || "Passwords must match";
    }
  }
}
</script>

<style>
/* Mandatory fields are accompanied with a * after it's respective labels*/
.required label::after {
  content: "*";
  color: red;
}
</style>
