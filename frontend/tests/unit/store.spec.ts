import Vuex, { Store } from 'vuex';
import { createLocalVue } from '@vue/test-utils';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import * as api from '@/api/internal';
import { castMock } from './utils';

jest.mock('@/api/internal', () => ({
  deleteNotification: jest.fn(),
}));

const deleteNotification = castMock(api.deleteNotification);

describe('store.ts', () => {

  let store: Store<StoreData>;

  beforeEach(() => {
    const localVue = createLocalVue();
    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
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

  describe('deleteAllStagedEvents', () => {

    beforeEach(() => {
      store.commit('stageEventForDeletion', 17);
      store.commit('stageEventForDeletion', 90);
      store.commit('stageEventForDeletion', 22);
    });

    it('Removes all events from the list of events staged for deletion', async () => {
      expect(store.state.eventForDeletionIds.length).toBe(3);
      await store.dispatch('deleteAllStagedEvents');
      expect(store.state.eventForDeletionIds.length).toBe(0);
    });

    it('Calls API endpoint to permenantly delete events once for each event in the list', async () => {
      const callTimesBefore = deleteNotification.mock.calls.length;
      await store.dispatch('deleteAllStagedEvents');
      expect(deleteNotification).toBeCalledTimes(callTimesBefore + 3);
    });

  });

});