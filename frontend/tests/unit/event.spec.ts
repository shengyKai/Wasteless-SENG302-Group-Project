
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import Event from '@/components/home/newsfeed/Event.vue';
import * as api from '@/api/internal';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { castMock } from './utils';
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  deleteNotification: jest.fn(),
}));

jest.mock('@/components/utils/Methods/synchronizedTime', () => ({
  now : new Date("2021-01-02T11:00:00Z")
}));

const deleteNotification = castMock(api.deleteNotification);

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @returns The generated user
 */
function makeTestUser(userId: number) {
  return {
    id:  userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: {
      streetNumber: 'test_street_number',
      streetName: 'test_street1',
      city: 'test_city',
      region: 'test_region',
      postcode: 'test_postcode',
      district: 'test_district',
      country: 'test_country' + userId
    },
    businessesAdministered: [],
  };
}

describe('Event.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  // The global store to be used
  let store: Store<StoreData>;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = makeTestUser(1);

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append (app);

    wrapper = mount(Event, {
      localVue,
      vuetify,
      store,
      propsData: {
        event: {
          id: 44,
          created: "2021-01-01T12:00:00Z",
        },
        title: "Test event",
      }
    });
  });

  describe('Event is not been set to be deleted', () => {

    beforeEach(async () => {
      await wrapper.setData({
        deleted: false,
      });
    });

    it('When deletion is finalized, method to permanently delete event is not called', async () => {
      await wrapper.vm.finalizeDeletion();
      expect(deleteNotification).toBeCalledTimes(0);
    });

    it('When user tries to delete event, event is set to be deleted', async () => {
      await wrapper.vm.initiateDeletion();
      expect(wrapper.vm.deleted).toBeTruthy();
    });

  });

  describe('Event has been set to be deleted', () => {

    beforeEach(async () => {
      await wrapper.setData({
        deleted: true,
      });
    });

    it('Remaining time is difference between current time and 10 secs after deletion time if this is positive', async () => {
      await wrapper.setData({
        deletionTime: new Date(synchronizedTime.now.getTime() - 8000),
      });
      expect(wrapper.vm.remainingTime).toBe(2);
    });

    it('Remaining time is zero if difference between current time and 10 secs after deletion time is negative', async () => {
      await wrapper.setData({
        deletionTime: new Date(synchronizedTime.now.getTime() - 20000),
      });
      expect(wrapper.vm.remainingTime).toBe(0);
    });

    it('When deletion is finalized, method to permanently delete event is called', async () => {
      store.commit('stageEventForDeletion', wrapper.vm.event.id);
      await wrapper.vm.finalizeDeletion();
      expect(deleteNotification).toBeCalledTimes(1);
    });

    it('When user tries to undo deletion, event is no longer set to be deleted', async () => {
      await wrapper.vm.undoDelete();
      expect(wrapper.vm.deleted).toBeFalsy();
    });

  });

});
