
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import Event from '@/components/home/newsfeed/Event.vue';
import * as api from '@/api/internal';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { castMock, makeTestUser } from '../utils';
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  deleteNotification: jest.fn(),
}));

jest.mock('@/components/utils/Methods/synchronizedTime', () => ({
  now : new Date("2021-01-02T11:00:00Z")
}));

const deleteNotification = castMock(api.deleteNotification);

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

    it('When an error message is passed to the event it is displayed', async () => {
      await wrapper.setProps({
        error: 'test_error_message',
      });
      await Vue.nextTick();
      expect(wrapper.text()).toContain('test_error_message');
    });

    it('When a error message is passed to the event and then removed it is not displayed', async () => {
      await wrapper.setProps({
        error: 'test_error_message',
      });
      await Vue.nextTick();
      await wrapper.setProps({
        error: undefined,
      });
      await Vue.nextTick();
      expect(wrapper.text()).not.toContain('test_error_message');
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