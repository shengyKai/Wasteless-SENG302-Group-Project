
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import MessageEvent from '@/components/home/newsfeed/MessageEvent.vue';
import MarketplaceCard from "@/components/cards/MarketplaceCard.vue";
import * as api from '@/api/internal';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { castMock, makeTestUser } from '../utils';

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  extendMarketplaceCardExpiry: jest.fn(),
}));

jest.mock('@/components/utils/Methods/synchronizedTime', () => ({
  now : new Date("2021-01-02T11:00:00Z")
}));

const extendMarketplaceCardExpiry = castMock(api.extendMarketplaceCardExpiry);

describe('MessageEvent.vue', () => {
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

    wrapper = mount(MessageEvent, {
      localVue,
      vuetify,
      store,
      components: {
        MarketplaceCard
      },
      propsData: {
        event: {
          type: 'MessageEvent',
          id: 7,
          tag: 'none',
          created: new Date().toString(),
          message: {
            id: 9,
            senderId: 100,
            created: '',
            content: 'test message',
          },
          conversation: {
            card: testMarketPlaceCard,
            buyer: testUser,
            id: 3,
          },
          participantType: 'seller',
        },
      }
    });
  });

  /**
     * Finds the delay expiry button in the MessageEvent.
     *
     * @returns A Wrapper around the delay expiry.
     */
  function findSendButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Send'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
     * Finds the button which controls whether the component showing the card is expanded or hidden.
     *
     * @returns A Wrapper around the expand button.
     */
  function findLoadMoreButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Load More'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }


});
