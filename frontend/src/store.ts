import {User, getUser, Business} from './api';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, setCookie } from './utils';

type RoleType = { type: "user", value: User } | { type: "business", value: Business }

export type StoreData = {
  user: User | null,
  activeRole: RoleType | null,
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
        state.activeRole = { type: "user", value: payload };
        // If the payload contains a user ID, user is now logged in. Set their session cookie.
        if (payload.id) {
          deleteCookie(COOKIE.USER.toUpperCase());
          deleteCookie(COOKIE.USER.toLowerCase());
          setCookie(COOKIE.USER, payload.id);
        }
      },
      /**
       * Sets the current active role.
       * This is either a business or user.
       *
       * @param state Current state
       * @param payload The new role, may not be null
       */
      setActiveRole(state, payload: RoleType) {
        state.activeRole = payload;
      },

      logoutUser (state) {
        state.user = null;
        state.activeRole = null;
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
      isLoggedIn(state) {
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
            console.warn(response);
            return;
          }
          context.commit('setUser', response);
        });
      }
    }
  };
}

export default function createStore() {
  return new Vuex.Store(createOptions());
}
