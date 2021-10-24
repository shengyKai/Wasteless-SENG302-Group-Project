import {AnyEvent, deleteNotification, getEvents} from './api/events';
import Vuex, { Store, StoreOptions } from 'vuex';
import { COOKIE, deleteCookie, getCookie, isTesting, setCookie } from './utils';
import Vue from 'vue';
import {getUser, login, User} from "@/api/user";
import {Business} from "@/api/business";
import {InventoryItem} from "@/api/inventory";

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
   * Object representing the current logged in business.
   */
  business: Business | null,

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
   */
  eventMap: Record<number, AnyEvent>,
  /**
   * The id numbers of all events which have been staged for deletion.
   */
  eventForDeletionIds: number[],
};

function createOptions(): StoreOptions<StoreData> {
  return {
    state: {
      user: null,
      business: null,
      activeRole: null,
      globalError: null,
      createBusinessDialogShown: false,
      createInventoryDialog: undefined,
      createSaleItemDialog: undefined,
      eventForDeletionIds: [],
      eventMap: {},
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
      setBusiness(state, payload: Business) {
        state.business = payload;
      },
      /**
       * Remove all of the events from the eventMap
       * @param state Current state
       */
      clearEvents(state) {
        state.eventMap = {};
      },
      /**
       * Adds or replaces a event in the event list
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
        state.eventMap = {};
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
       * @param saleItemInfo sale item details
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
       * Add an event id to the list of event ids to be stored until the events are permenatly deleted.
       * Events on this list will be deleted when the browser/tab closes.
       */
      stageEventForDeletion(state, eventId: number) {
        state.eventForDeletionIds.push(eventId);
      },
      /**
       * Remove an event id from the list of event ids to be stored until the events are permenatly deleted,
       * so that it is not deleted when the browser/tab closes.
       */
      unstageEventForDeletion(state, eventId: number) {
        state.eventForDeletionIds = state.eventForDeletionIds.filter(id => id !== eventId);
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
      /**
       * Returns true if there are any events in the list of events staged for deletion, otherwise returns false.
       * @param state Current state of the store.
       * @returns True if there are events staged for deletion, false otherwise.
       */
      areEventsStaged(state) {
        return state.eventForDeletionIds.length !== 0;
      },
    },
    actions: {
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
          return;
        }
        context.commit('setUser', response);

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

        return undefined;
      },
      /**
       * If the event is in the list of events staged for deletion, send a request to the backend to permenantly
       * delete the event. Otherwise return an error message.
       * @param context The current context of the store.
       * @param eventId The id number of the event to be deleted.
       * @returns An error message or undefined.
       */
      async deleteStagedEvent(context, eventId: number) {
        // Check that the event has been staged for deletion
        const eventToDeleteId = context.state.eventForDeletionIds.filter(id => id === eventId);
        if (eventToDeleteId.length === 1) {
          // Remove event from list of events staged for deletion
          context.state.eventForDeletionIds = context.state.eventForDeletionIds.filter(id => id !== eventId);
          const response = await deleteNotification(eventId);
          if (typeof response === 'string') {
            return response;
          } else {
            context.commit('removeEvent', eventId);
            return undefined;
          }
        }
        return 'Notification not staged for deletion';
      },
      /**
       * Sends a request to get all the events which should be present in the user's newsfeed and adds
       * them to the eventMap. If the are already events in the eventMap, only requests events which
       * have been modified after the most recently modified event.
       * @param context The store context.
       */
      async refreshEventFeed(context) {
        const userId = context.state.user?.id;
        if (!userId) return;
        let response = await getEvents(userId, undefined);
        if (typeof response === 'string') {
          console.error(response);
        } else {
          context.commit('clearEvents');
          for (let event of response) {
            context.commit('addEvent', event);
          }
        }
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
