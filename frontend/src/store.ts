import {User,  Business, getUser, login} from './api';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, setCookie } from './utils';

export type StoreData = {
  user: User | null,
  activeRole: { type: "user" | "business", id: number} | null,
  createBusinessDialogShown: boolean,
};

export function createOptions(): StoreOptions<StoreData> {
  return {
    state: {
      user: null,
      activeRole: null,
      createBusinessDialogShown: false,
    },
    mutations: {
      setUser (state, payload: User) {
        state.user = payload;

        // Ensures that when we log in we always have a role.
        // Maybe it will be worth considering in the future persistently remembering the previous role
        state.activeRole = { type: "user", id: payload.id };

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
      },

      /**
       * Creates a modal create business dialog
       *
       * @param state Current store state
       */
      showCreateBusiness(state) {
        state.createBusinessDialogShown = true;
      },

      /**
       * Hides the create business dialog
       *
       * @param state Current store state
       */
      hideCreateBusiness(state) {
        state.createBusinessDialogShown = false;
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
      async login(context, { email, password }) {
        let userId = await login(email, password);
        if (typeof userId === 'string') {
          console.warn(userId);
          return;
        }
        let user = await getUser(userId);
        if (typeof user === 'string') {
          console.error(user);
          return;
        }
        context.commit('setUser', user);
      }
    }
  };
}

export default function createStore() {
  return new Vuex.Store(createOptions());
}
