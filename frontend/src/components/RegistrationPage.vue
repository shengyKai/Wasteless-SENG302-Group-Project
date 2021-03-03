<template>
  <div>
    <img class="profile-img" src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg"/>
  </div>
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
  border-radius: 50%;
}
</style>