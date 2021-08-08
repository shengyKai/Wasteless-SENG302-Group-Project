import { User, Business, getUser, login, InventoryItem, deleteNotification } from './api/internal';
import { AnyEvent, initialiseEventSourceForUser, addEventMessageHandler } from './api/events';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, getCookie, isTesting, setCookie } from './utils';
import Vue from 'vue';

type UserRole = { type: "user" | "business", id: number };
type SaleItemInfo = { businessId: number, inventoryItem: InventoryItem };

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
  activeRole: UserRole | null,

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
  /**
   * Whether or not the dialog for registering a business is being shown.
   */
  createInventoryDialog: number | undefined,
  /**
   * Whether or not the dialog for registering a business is being shown.
   */
  createSaleItemDialog: SaleItemInfo | undefined,
  /**
   * Map from event ids to events.
   * This is a sparse array
   */
  eventMap: Record<number, AnyEvent>,
  /**
   * A record of all the events which have been temporarily deleted on the frontend, but have
   * not been permanently deleted on the backend as the 10 second window for reversing deletion
   * has not passed.
   */
  temporaryDeletedEvents: AnyEvent[],
};

function createOptions(): StoreOptions<StoreData> {
  return {
    state: {
      user: null,
      activeRole: null,
      globalError: null,
      createBusinessDialogShown: false,
      createInventoryDialog: undefined,
      createSaleItemDialog: undefined,
      eventMap: [],
      temporaryDeletedEvents: [],
    },
    mutations: {
      setUser(state, payload: User) {
        state.user = payload;

        // Ensures that when we log in we always have a role.
        state.activeRole = { type: "user", id: payload.id };

        // If the payload contains a user ID, user is now logged in. Set their session cookie.
        if (payload.id) {
          deleteCookie(COOKIE.USER.toUpperCase());
          deleteCookie(COOKIE.USER.toLowerCase());
          setCookie(COOKIE.USER, payload.id);
        }
      },
      /**
       * Adds or replaces a event in the event list
       * This method is only expected to be called from the event message handler
       * @param state Current state
       * @param payload New event
       */
      addEvent(state, payload: AnyEvent) {
        Vue.set(state.eventMap, payload.id, payload);
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
       * Dismisses the current error message that is displayed.
       * This function is only expected to be called from the global error message component.
       *
       * @param state Current state
       */
      clearError(state) {
        state.globalError = null;
      },

      logoutUser(state) {
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
      },
      /**
       * Creates a modal create inventory dialog for adding a sale item to the provided business
       *
       * @param state Current store state
       * @param businessId Business to create the sale item for
       */
      showCreateSaleItem(state, saleItemInfo: SaleItemInfo) {
        state.createSaleItemDialog = saleItemInfo;
      },
      /**
       * Hides the create inventory dialog
       *
       * @param state Current store state
       */
      hideCreateSaleItem(state) {
        state.createSaleItemDialog = undefined;
      },

      /**
       * Sets the current user role
       *
       * @param state Current store state
       * @param role Role to act as
       */
      setRole(state, role: UserRole) {
        setCookie('role', JSON.stringify(role));
        state.activeRole = role;
      },
      /**
       * Removes an event based on a provided event id
       * @param state Current store state
       * @param id id of the event
       */
      removeEvent(state, id: number) {
        Vue.delete(state.eventMap, id);
      },
      /**
       * Add an event to the list of events to be stored until they are permenatly deleted.
       * Events on this list will be deleted when the browser/tab closes.
       */
      deleteEventTemporary(state, event: AnyEvent) {
        state.temporaryDeletedEvents.push(event);
      },
      /**
       * Remove an event to the list of events to be stored until they are permenatly deleted,
       * so that it is not deleted when the browser/tab closes.
       */
      restoreDeletedEvent(state, id: number) {
        state.temporaryDeletedEvents = state.temporaryDeletedEvents.filter(event => event.id !== id);
      }
    },
    getters: {
      isLoggedIn(state) {
        return state.user !== null;
      },
      role(state) {
        return state.user?.role;
      },
      /**
       * Gets a list of all events sorted by creation date
       * @param state Current state
       * @returns List of events
       */
      events(state) {
        let events = Object.values(state.eventMap);
        events.sort((a, b) => +new Date(b.created) - +new Date(a.created));
        return events;
      },
    },
    actions: {
      /**
       * Starts listening to notification events which are placed in state.eventMap
       * This is expected to be called just after logging in
       * @param context The store context
       */
      startUserFeed(context) {
        context.state.eventMap = []; // Clear events
        initialiseEventSourceForUser(context.state.user!.id); // Make event handler
        addEventMessageHandler(event => context.commit('addEvent', event));
      },
      /**
       * Attempts to automatically log in the provided user id with the current authentication cookies.
       * Will also set the current role to the previously selected role.
       *
       * @param context The store context
       * @param userId The userId to try to login as
       */
      async autoLogin(context, userId: number) {
        const response = await getUser(userId);
        if (typeof response === 'string') {
          //context.commit('setError', response);
          return;
        }
        context.commit('setUser', response);
        context.dispatch('startUserFeed');

        let rawRole = getCookie('role');
        if (rawRole !== null) {
          rawRole = rawRole.split('=')[1];

          const role: UserRole = JSON.parse(rawRole);
          // We should already be logged in at this point, so this should be valid
          const user = context.state.user!;

          if (role.type === 'user') {
            if (user.id === role.id) {
              context.state.activeRole = role;
            } else {
              console.warn('Previous role id does not match current user id');
            }
          } else if (role.type === 'business') {
            let success = false;
            for (const business of (user.businessesAdministered || [])) {
              if (business.id !== role.id) continue;
              context.state.activeRole = role;
              success = true;
              break;
            }
            if (!success) {
              console.warn(`Previous role id does not match a administered business id=${role.id}`);
            }
          } else {
            console.error(`Unknown role type: "${role.type}"`);
          }
        }
      },
      /**
       * Attempts to log in the given user.
       * This will set the cookies and with authenticate future requests.
       *
       * @param context The current context
       * @param Object containing the login credentials
       * @returns Undefined if successful or a string error message
       */
      async login(context, { email, password }) {
        let userId = await login(email, password);
        if (typeof userId === 'string') {
          return userId;
        }
        let user = await getUser(userId);
        if (typeof user === 'string') {
          return user;
        }
        context.commit('setUser', user);
        context.dispatch('startUserFeed');

        return undefined;
      },
      /**
       * If the event is in the list of temporarily deleted events, send a request to the backend to permenantly
       * delete the event. Otherwise return an error message.
       * @param id The id number of the event to be deleted.
       * @returns An error message or undefined.
       */
      async deleteEventPermenant(context, id: number) {
        const eventToDelete = context.state.temporaryDeletedEvents.filter(event => event.id === id);
        if (eventToDelete.length === 1) {
          // Event must be removed from list of temporary deleted events even if request is not successful
          // as if request is unsuccessful event notification will reappear on newsfeed.
          context.state.temporaryDeletedEvents = context.state.temporaryDeletedEvents.filter(event => event.id !== id);
          const response = await deleteNotification(id);
          if (typeof response === 'string') {
            return response;
          } else {
            return undefined;
          }
        }
        return 'Failed to delete notification';
      },
      async deleteAllEventsPermenant(context) {
        for (let event of context.state.temporaryDeletedEvents) {
          deleteNotification(event.id);
        }
        context.state.temporaryDeletedEvents = [];
      }
    }
  };
}


let store: Store<StoreData>;
if (!isTesting()) {
  // If we're in a test enviroment then Vue.use(Vuex) won't have been called yet.
  store = new Vuex.Store(createOptions());
}

/**
 * Resets the global store to the initial state.
 * This function is only to be used in the test enviroment
 */
export function resetStoreForTesting() {
  if (!isTesting()) throw new Error('This function should only be called when testing');
  store = new Vuex.Store(createOptions());
}

/**
 * Gets the global Vuex store.
 *
 * @returns The global Vuex store
 */
export function getStore() {
  return store;
}
