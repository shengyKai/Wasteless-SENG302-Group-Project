<template>
  <v-main>
    <v-card class="body">
      <div>
        <div class="profile-img">
          <v-avatar size="200px">
            <img src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg"/>
          </v-avatar>
        </div>
        <div class="names">
          <h1>
            {{ fullName }}
          </h1>
          <h2><i>
            {{ nickname }}
          </i></h2>
        </div>
      </div>

      <div class="fields">
        <v-simple-table>
          <tbody>
            <tr>
              <td>
                Email
              </td>
              <td>
                {{ email }}
              </td>
              <td>
                Date of Birth
              </td>
              <td>
                {{ dateOfBirth }}
              </td>
            </tr>
            <tr>
              <td>
                Phone Number
              </td>
              <td>
                {{ phoneNo }}
              </td>
              <td>
                Home Address
              </td>
              <td>
                {{ address }}
              </td>
            </tr>
            </tbody>
        </v-simple-table>
      </div>

      <div>
        {{ bio }}
      </div>
    </v-card>
  </v-main>
</template>

<script lang="ts">

// app Vue instance
const RegistrationPage = {
  name: "RegistrationPage",

  // app initial state
  data: function () {
    return {
    };
  },

  mounted() {
  },

  props: ['fullName', 'nickname', 'bio', 'email', 'dateOfBirth', 'phoneNo', 'address'], 
  // props: ['id'], 
  // TODO fetch properties from the store instead

  // computed properties
  // http://vuejs.org/guide/computed.html
  computed: {
  },

  filters: {
  },

  // methods that implement data logic.
  // note there's no DOM manipulation here at all.
  methods: {
  },

  // a custom directive to wait for the DOM to be updated
  // before focusing on the input field.
  // http://vuejs.org/guide/custom-directive.html
  directives: {
  },
};

export default RegistrationPage;
</script>

<style scoped>
.profile-img {
  float: left;
  margin-top: -116px;
  margin-right: 16px;
}

.body {
  padding: 16px;
  width: 100%;
}

.names {
  margin-left: auto;
  margin-right: auto;
}

.fields {
  clear: both;
}

td:nth-child(1),td:nth-child(3) {
  font-weight: bold;
  text-align: right;
}

tbody tr:hover {
  background-color: transparent !important;
}

</style>