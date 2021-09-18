import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import Event from '@/components/home/newsfeed/Event.vue';
import * as eventsApi from '@/api/events';

import Vuex, {Store} from 'vuex';
import {getStore, resetStoreForTesting, StoreData} from '@/store';
import {castMock, makeTestUser} from '../utils';
import synchronizedTime from '@/components/utils/Methods/synchronizedTime';
import {deleteNotification as deleteNotification1} from "@/api/internal-event";

Vue.use(Vuetify);

jest.mock('@/api/internal-event', () => ({
  deleteNotification: jest.fn(),
}));

jest.mock('@/api/events', () => ({
  getEvents: jest.fn(),
  updateEventAsRead: jest.fn(),
  updateEventStatus: jest.fn(),
}));

jest.mock('@/components/utils/Methods/synchronizedTime', () => ({
  now : new Date("2021-01-02T11:00:00Z")
}));

const deleteNotification = castMock(deleteNotification1);
const updateEventAsRead = castMock(eventsApi.updateEventAsRead);
const updateEventStatus = castMock(eventsApi.updateEventStatus);
const getEvents = castMock(eventsApi.getEvents);

describe('Event.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  // The global store to be used
  let store: Store<StoreData>;

  const localVue = createLocalVue();

  function generateWrapper() {

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
          status: "normal",
          id: 44,
          created: "2021-01-01T12:00:00Z"
        },
        title: "Test event",
      }
    });

    getEvents.mockResolvedValue([]);
  }

  beforeEach(async () => {
    generateWrapper();
  });

  /**
   * Finds the associated icon in the event/notification
   *
   * @returns A wrapper around the update icon
   */
  function findIcon(component:string) {
    const icons = wrapper.findAllComponents({ name: 'v-icon' });
    const icon = icons.filter(icon => icon.attributes().class.includes(component));
    expect(icon.length).toBe(1);
    return icon.at(0);
  }


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

  describe('Event is marked as read', () => {
    beforeEach(() => {
      generateWrapper();
      updateEventAsRead.mockClear();
    });

    afterEach(() => {
      wrapper.destroy();
    });

    it("On click of the event component, the api endpoint to update the read status is called", async () => {
      wrapper.trigger("click");
      await Vue.nextTick();
      expect(updateEventAsRead).toHaveBeenCalled();
    });

    it("If the event is already marked as read, the api endpoint to update the read status will not be called", async () => {
      await wrapper.setData({
        event: {
          read: true
        }
      });
      wrapper.trigger("click");
      await Vue.nextTick();
      expect(updateEventAsRead).not.toHaveBeenCalled();
    });
  });

  describe('Icons that rendered on notification when status is normal', () => {
    beforeEach(async () => {
      generateWrapper();
      await wrapper.setData({
        event: {
          status: 'normal'
        }
      });
      updateEventStatus.mockClear();
    });

    afterEach(() => {
      wrapper.destroy();
    });

    it("Archive icon is rendered, api endpoint was called upon clicking", async () => {
      const archiveButton = findIcon("archive");
      expect(archiveButton.exists).toBeTruthy;
      await archiveButton.trigger('click');
      await Vue.nextTick();
      expect(updateEventStatus).toHaveBeenCalled();
    });

    it("Outlined-Star icon is rendered, api endpoint was called upon clicking", async () => {
      const outLinedStarButton = findIcon("star-outline");
      expect(outLinedStarButton.exists).toBeTruthy;
      await outLinedStarButton.trigger('click');
      await Vue.nextTick();
      expect(updateEventStatus).toHaveBeenCalled();
    });

    it("Star icon was not rendered, api endpoint was not called", async () => {
      const icons = wrapper.findAllComponents({ name: 'v-icon' });
      const icon = icons.filter(icon => icon.attributes().class.includes('star'));
      expect(icon.exists).toBeFalsy;
      await Vue.nextTick();
      expect(updateEventStatus).not.toHaveBeenCalled();
    });
  });

  describe('Icons that rendered on notification when status is starred', () => {
    beforeEach(async () => {
      generateWrapper();
      await wrapper.setData({
        event: {
          status: 'starred'
        }
      });
      updateEventStatus.mockClear();
    });

    afterEach(() => {
      wrapper.destroy();
    });

    it("Archive icon is rendered, api endpoint was called upon clicking", async () => {
      const archiveButton = findIcon("archive");
      expect(archiveButton.exists).toBeTruthy;
      await archiveButton.trigger('click');
      await Vue.nextTick();
      expect(updateEventStatus).toHaveBeenCalled();
    });

    it("Outlined-Star icon was not rendered, api endpoint was not called", async () => {
      const icons = wrapper.findAllComponents({ name: 'v-icon' });
      const icon = icons.filter(icon => icon.attributes().class.includes('star-outline'));
      expect(icon.exists).toBeFalsy;
      await Vue.nextTick();
      expect(updateEventStatus).not.toHaveBeenCalled();
    });

    it("Star icon is rendered, api endpoint was called upon clicking", async () => {
      const archiveButton = findIcon("star");
      expect(archiveButton.exists).toBeTruthy;
      await archiveButton.trigger('click');
      await Vue.nextTick();
      expect(updateEventStatus).toHaveBeenCalled();
    });
  });

  describe('Icons that rendered on notification when status is archived', () => {
    beforeEach(async () => {
      generateWrapper();
      await wrapper.setData({
        event: {
          status: 'archived'
        }
      });
      updateEventStatus.mockClear();
    });

    afterEach(() => {
      wrapper.destroy();
    });

    it("Archive icon is not rendered, api endpoint was not called", async () => {
      const icons = wrapper.findAllComponents({ name: 'v-icon' });
      const icon = icons.filter(icon => icon.attributes().class.includes('archive-outline'));
      expect(icon.exists).toBeFalsy;
      await Vue.nextTick();
      expect(updateEventStatus).not.toHaveBeenCalled();
    });

    it("Outlined-Star icon was not rendered, api endpoint was not called", async () => {
      const icons = wrapper.findAllComponents({ name: 'v-icon' });
      const icon = icons.filter(icon => icon.attributes().class.includes('star-outline'));
      expect(icon.exists).toBeFalsy;
      await Vue.nextTick();
      expect(updateEventStatus).not.toHaveBeenCalled();
    });

    it("Star icon was not rendered, api endpoint was not called", async () => {
      const icons = wrapper.findAllComponents({ name: 'v-icon' });
      const icon = icons.filter(icon => icon.attributes().class.includes('star'));
      expect(icon.exists).toBeFalsy;
      await Vue.nextTick();
      expect(updateEventStatus).not.toHaveBeenCalled();
    });

    it("Trash can icon is rendered, initiateDeletion() was called upon clicking", async () => {
      const deleteButton = findIcon("trash-can");
      expect(deleteButton.exists).toBeTruthy;
      await deleteButton.trigger('click');
      await Vue.nextTick();
      expect(wrapper.vm.deleted).toBeTruthy();
    });
  });

});
