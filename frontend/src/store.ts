import { User, getUser } from './api';
import Vuex from 'vuex';
import { COOKIE, deleteCookie, setCookie } from './utils';

type StoreData = {
  user: User | null,
};

const store = new Vuex.Store<StoreData>({
  state: {
    user: null,
  },
  mutations: {
    setUser (state, payload: User) {
      state.user = payload;
      if (payload.id) setCookie(COOKIE.USER, payload.id)
    },
    logoutUser (state) {
      state.user = null;
      deleteCookie(COOKIE.USER);
    }
  },
  getters: {
    isLoggedIn (state) {
      return state.user !== null;
    }
  },
  actions: {
    getUser (context) {
      return getUser().then((response) => {
        if (typeof response === 'string') {
          console.warn(response);
          return;
        }
        context.commit('setUser', response);
      });
    }
  }
});

export default store;
