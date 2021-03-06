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
        @keyup="passwordChange"
        :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :type="showPassword ? 'text' : 'password'"
        @click:append="showPassword = !showPassword"
        :rules="mandatoryRules"
        outlined
      />

      <!-- INPUT: Confirm Password -->
      <v-text-field
        ref="confirmPassword"
        class="required"
        v-model="confirmPassword"
        label="Confirm Password"
        :append-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :type="showConfirmPassword ? 'text' : 'password'"
        @click:append="showConfirmPassword = !showConfirmPassword"
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
      <v-autocomplete
        class="required"
        v-model="address"
        label="Address"
        :rules="mandatoryRules"
        :items="items"
        :loading="isLoading"
        :search-input.sync="search"
        item-text="name"
        item-value="symbol"
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
      showPassword: false,
      showConfirmPassword: false,
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
        //regex rules for emails, example format is as such:
        //"blah@hotmail.co
        //if it does not follow the format, display error message
        email => /^\w+([.-]?\w+)*@\w+([.-]?\w+)*(\.\w{2,3})+$/.test(email) || 'E-mail must be valid'
      ],
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field =>  !!field || 'Field is required'
      ],
      modal: false,
      items: [],
      isLoading: false,
      search: null,
    }
  },


  watch: {
    search () {
      // Items have already been loaded
      if (this.items.length > 0) return
      this.isLoading = true
      // Lazily load input items
      fetch('https://api.coingecko.com/api/v3/coins/list')
          .then(res => res.clone().json())
          .then(res => {
            this.items = res
          })
          .catch(err => {
            console.log(err)
          })
          .finally(() => (this.isLoading = false))
    },
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
    },
    //Feature bug:
    //After the user has successfully typed in the same values in the password and confirmPassword, if the user decides
    //to change the password field value again first without editing the confirmPassword field after, because it has
    //been validated once before, the form would recognize both fields to be valid. That is not what is wanted from
    //this form validation.
    //Feature fix:
    //The bottom method solves that issue by observing the password field. That means the @keyup attribute in the
    //password field observes every finished keystroke in there and calls the ref with "confirmPassword" (in this case
    //it refers to the confirmPassword field)to revalidate itself upon any changes in the password field.
    passwordChange() {
      this.$refs.confirmPassword.validate();
    },
    querySelections (v) {
      this.loading = true
      // Simulated ajax query
      setTimeout(() => {
        this.items = this.states.filter(e => {
          return (e || '').toLowerCase().indexOf((v || '').toLowerCase()) > -1
        })
        this.loading = false
      }, 500)
    }
  },
  computed: {
    //The computed property below is dependent on two user input fields, password and password confirmation.
    //After the user has typed in the password field, the confirmPassword field would check this rule for each
    //change(in this case, each keystroke), and compare it with the password field. If they are not the same,
    //the error message "Passwords must match" will show up at the bottom of the confirmPassword field, until it
    //is the same.
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
