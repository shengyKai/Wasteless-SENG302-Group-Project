import {User, getUser, Business} from './api';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, setCookie } from './utils';

export type StoreData = {
  /**
   * Object representing the current logged in user.
   * If null then no user is logged in
   */
  user: User | null,

  /**
   * The current role the user is acting as.
   * If acting as user then the user is shown content related to their personal account.
   * If acting as a business then the user is shown content related to their business.
   */
  activeRole: { type: "user" | "business", id: number} | null,

  /**
   * The global error message that is displayed at the top of the screen.
   *
   * This is the most intruding method for displaying errors and should only be used for errors that 
   * must interrupt the application flow.
   * Otherwise error messages should be displayed closer to where they are generated from.
   */
  globalError: string | null,

  /**
   * Whether or not the dialog for registering a business is being shown.
   */
  createBusinessDialogShown: boolean,
};

export function createOptions(): StoreOptions<StoreData> {
  return {
    state: {
      user: null,
      activeRole: null,
      globalError: null,
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
      /**
       * Displays an error message at the top of the screen.
       *
       * This is the least perferred method for displaying errors.
       * Since error messages should be displayed closer to where they are generated from.
       *
       * @param state Current state
       * @param error Error message to display
       */
      setError(state, error: string) {
        state.globalError = error;
      },
      /**
       * Dismisses the current error message that is display.
       * This function is only expected to be called from the global error message component.
       *
       * @param state Current state
       */
      clearError(state) {
        state.globalError = null;
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
      getUser (context) {
        return getUser().then((response) => {
          if (typeof response === 'string') {
            context.commit('setError', response);
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
