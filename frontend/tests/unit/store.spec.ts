import Vuex, {Store} from 'vuex';
import {createLocalVue} from '@vue/test-utils';
import {getStore, resetStoreForTesting, StoreData} from '@/store';
import {castMock} from './utils';
import * as events from '@/api/events';

jest.mock('@/api/events', () => ({
  getEvents: jest.fn(),
  deleteNotification: jest.fn(),
}));

const deleteNotification = castMock(events.deleteNotification);
const getEvents = castMock(events.getEvents);

describe('store.ts', () => {

  let store: Store<StoreData>;

  beforeEach(() => {
    const localVue = createLocalVue();
    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
  });

  it('areEventsStaged returns false if no events have been added to the list of events to be deleted', () => {
    expect(store.state.eventForDeletionIds.length).toBe(0);
    expect(store.getters.areEventsStaged).toBeFalsy();
  });

  it('areEventsStaged returns true if one event have been added to the list of events to be deleted', () => {
    store.commit('stageEventForDeletion', 100);
    expect(store.state.eventForDeletionIds.length).toBe(1);
    expect(store.getters.areEventsStaged).toBeTruthy();
  });

  it('areEventsStaged returns true if multiple events have been added to the list of events to be deleted', () => {
    store.commit('stageEventForDeletion', 18);
    store.commit('stageEventForDeletion', 31);
    store.commit('stageEventForDeletion', 44);
    expect(store.state.eventForDeletionIds.length).toBe(3);
    expect(store.getters.areEventsStaged).toBeTruthy();
  });


  describe('deleteStagedEvent', () => {

    it('Returns error message if event is not staged for deletion', async () => {
      expect(store.state.eventForDeletionIds.includes(100)).toBeFalsy();
      const response = await store.dispatch('deleteStagedEvent', 100);
      expect(response).toEqual('Notification not staged for deletion');
    });

    it('Removes event from list of events staged for deleteion if event is on list', async () => {
      store.commit('stageEventForDeletion', 100);
      expect(store.state.eventForDeletionIds.includes(100)).toBeTruthy;
      await store.dispatch('deleteStagedEvent', 100);
      expect(store.state.eventForDeletionIds.includes(100)).toBeFalsy();
    });

    it('Calls API endpoint to permenantly delete event if event staged for deletion', async () => {
      const callTimesBefore = deleteNotification.mock.calls.length;
      store.commit('stageEventForDeletion', 100);
      expect(store.state.eventForDeletionIds.includes(100)).toBeTruthy;
      await store.dispatch('deleteStagedEvent', 100);
      expect(deleteNotification).toBeCalledTimes(callTimesBefore + 1);
    });

    it('Returns undefined if API request successful', async () => {
      deleteNotification.mockResolvedValueOnce(undefined);
      store.commit('stageEventForDeletion', 100);
      expect(store.state.eventForDeletionIds.includes(100)).toBeTruthy;
      const response = await store.dispatch('deleteStagedEvent', 100);
      expect(response).toBe(undefined);
    });

    it('Returns error message if API request unsuccessful', async () => {
      deleteNotification.mockResolvedValueOnce('This is an error message');
      store.commit('stageEventForDeletion', 100);
      expect(store.state.eventForDeletionIds.includes(100)).toBeTruthy;
      const response = await store.dispatch('deleteStagedEvent', 100);
      expect(response).toBe('This is an error message');
    });

  });

  describe('refreshEventFeed', () => {

    beforeEach(() => {
      getEvents.mockClear();
      getEvents.mockResolvedValue([]);
      store.state.user = {
        id: 6,
        firstName: "First",
        lastName: "Last",
        email: "email@email.com",
        homeAddress: {
          country: "Country"
        },
        images: [],
      };
    });

    describe('eventMap contains no events', () => {

      beforeEach(() => {
        expect(Object.keys(store.state.eventMap).length).toBe(0);
      });

      it('Calls getEvents method with id of current user and undefined for date parameter if there are no events in eventMap', async() => {
        await store.dispatch('refreshEventFeed');
        expect(getEvents).toBeCalledTimes(1);
        expect(getEvents).toBeCalledWith(6, undefined);
      });

    });

    describe('eventMap contains events', () => {

      let event1 : events.GlobalMessageEvent;
      let event2 : events.GlobalMessageEvent;
      let event3 : events.GlobalMessageEvent;

      beforeEach(() => {
        event1 = {
          type: 'GlobalMessageEvent',
          status: 'archived',
          id: 7,
          tag: 'none',
          read: false,
          created: '2021-07-15T05:10:00Z',
          message: 'First',
          lastModified: '2021-07-15T05:10:00Z',
        };
        store.commit('addEvent', event1);
        event2 = {
          type: 'GlobalMessageEvent',
          status: 'archived',
          id: 14,
          tag: 'none',
          read: false,
          created: '2021-07-15T05:10:00Z',
          message: 'Second',
          lastModified: '2021-11-15T05:10:00Z',
        };
        store.commit('addEvent', event2);
        event3 = {
          type: 'GlobalMessageEvent',
          status: 'archived',
          id: 21,
          tag: 'none',
          read: false,
          created: '2021-07-15T05:10:00Z',
          message: 'Third',
          lastModified: '2021-09-15T05:10:00Z',
        };
        store.commit('addEvent', event3);
        expect(Object.keys(store.state.eventMap).length).toBe(3);
      });

      it('Calls getEvents method with id of current user and latest lastModified date from events if there are events in eventMap', async() => {
        await store.dispatch('refreshEventFeed');
        expect(getEvents).toBeCalledTimes(1);
        expect(getEvents).toBeCalledWith(6, '2021-11-15T05:10:00Z');
      });

      it('Adds returned events to eventMap if response from getEvents is a list of events', async () => {
        const returnedEvents: events.AnyEvent[] = [
          {
            type: 'GlobalMessageEvent',
            status: 'archived',
            id: 12,
            tag: 'none',
            read: false,
            created: '2021-07-15T05:10:00Z',
            message: 'Third',
            lastModified: '2021-09-15T05:10:00Z',
          },
          {
            type: 'GlobalMessageEvent',
            status: 'archived',
            id: 33,
            tag: 'none',
            read: false,
            created: '2021-07-15T05:10:00Z',
            message: 'Third',
            lastModified: '2021-09-15T05:10:00Z',
          }
        ];
        getEvents.mockResolvedValueOnce(returnedEvents);
        await store.dispatch('refreshEventFeed');
        expect(Object.keys(store.state.eventMap).length).toBe(5);
        expect(store.state.eventMap[12]).toStrictEqual(returnedEvents[0]);
        expect(store.state.eventMap[33]).toStrictEqual(returnedEvents[1]);
      });

      it('eventMap does not change if response from getEvents is a list of events', async () => {
        console.error = jest.fn(); // Console error should be printed in this test, so suppress it to avoid confusion in reading test output
        getEvents.mockResolvedValueOnce('An error has occured');
        await store.dispatch('refreshEventFeed');
        expect(Object.keys(store.state.eventMap).length).toBe(3);
        expect(store.state.eventMap[7]).toStrictEqual(event1);
        expect(store.state.eventMap[14]).toStrictEqual(event2);
        expect(store.state.eventMap[21]).toStrictEqual(event3);
      });

    });

  });

});