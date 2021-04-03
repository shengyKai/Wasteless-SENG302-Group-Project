import {User, getUser, login} from './api';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, setCookie } from './utils';

export type StoreData = {
  user: User | null,
};

export function createOptions(): StoreOptions<StoreData> {
  return {
    state: {
      user: null,
    },
    mutations: {
      setUser (state, payload: User) {
        state.user = payload;
        // If the payload contains a user ID, user is now logged in. Set their session cookie.
        if (payload.id) {
          deleteCookie(COOKIE.USER.toUpperCase());
          deleteCookie(COOKIE.USER.toLowerCase());
          setCookie(COOKIE.USER, payload.id);
        }
      },
      logoutUser (state) {
        state.user = null;
        deleteCookie(COOKIE.USER);
      }
    },
    getters: {
      isLoggedIn (state) {
        return state.user !== null;
      },
      role (state) {
        return state.user?.role;
      }
    },
    actions: {
      getUser (context) {
        return getUser().then((response) => {
          if (typeof response === 'string') {
            //console.warn(response);
            return;
          }
          context.commit('setUser', response);
        });
      },
      login (context, payload) {
        console.log('B');
        return login(payload.email, payload.password).then((response) => {
          console.log('C');
          console.log(payload.password);
          console.log(payload.email);
          if (typeof response === 'string') {
            console.warn(response);
            console.log(response);
            console.log('A');
            return;
          }
        });
      }
    }
  };
}

export default function createStore() {
  return new Vuex.Store(createOptions());
}
