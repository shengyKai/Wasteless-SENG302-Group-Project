<template>
  <div v-if="login">
    <Login @showRegister="togglePage" />
  </div>
  <div v-else>
    <Register @showLogin="togglePage" />
  </div>
</template>

<script>
import Register from './Register';
import Login from './Login';

export default {
  name: 'Auth',
  components: {
    Login,
    Register
  },
  data() {
    return {
      login: false
    }
  },
  methods: {
    togglePage() {
      this.login = !this.login;
    }
  }
}
</script>