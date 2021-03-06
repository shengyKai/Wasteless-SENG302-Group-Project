import axios from 'axios';
import Vuex from 'vuex';

export interface User {
  id: number|null;
  firstName: string|null;
  lastName: string|null;
  middleName: string|null;
  nickname: string|null;
  bio: string|null;
  email: string|null;
  dateOfBirth: string|null;
  phoneNumber: string|null;
  homeAddress: string|null;
  created: string|null;
  role: string|null;
  businessesAdministered: number[]|null;
}

var user: User = {
  id: null,
  firstName: null,
  lastName: null,
  middleName: null,
  nickname: null,
  bio: null,
  email: null,
  dateOfBirth: null,
  phoneNumber: null,
  homeAddress: null,
  created: null,
  role: null,
  businessesAdministered: null
}

const store = new Vuex.Store({
  state: {
    user
  },
  mutations: {
    setUser (state, payload: User) {
      state.user = payload;
    },
    logoutUser (state) {
      state.user = user;
    }
  },
  getters: {
    isLoggedIn (state) {
      return state.user.id !== null;
    }
  },
  actions: {
    getUser (context) {
      return axios.get('https://virtserver.swaggerhub.com/matthewminish/seng302-2021-api-spec/1.0.0/users/1')
        .then(res => context.commit('setUser', res.data))
        .catch(err => console.warn(err));
    }
  }
});

export default store;
