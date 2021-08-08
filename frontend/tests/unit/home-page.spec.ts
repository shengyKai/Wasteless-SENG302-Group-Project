import Vue from 'vue';
import Vuex, { Store } from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import MarketplaceCard from '@/components/cards/MarketplaceCard.vue';
import { deleteMarketplaceCard, MarketplaceCardSection, User } from '@/api/internal';
import HomePage from '@/components/home/HomePage.vue';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

import { AnyEvent } from '@/api/events';

Vue.use(Vuetify);
const localVue = createLocalVue();

const testUser: User = {
  id: 2,
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  email: 'test_email',
  homeAddress: { country: 'test_country', city: 'test_city', district: 'test_district'},
};

const testMarketplaceCard = {
  id: 1,
  creator: testUser,
  section: 'ForSale',
  created: '2021-03-10',
  lastRenewed: '2021-03-10',
  title: 'test_card_title',
  description: 'test_card_description',
  keywords: [{id: 3, name: 'test_keyword_1'}, {id: 4, name: 'test_keyword_2'}],
};

  /**
   * Creates the environment used for testing. The marketplace card being viewed
   * can be altered by changing the contents of the card
   */
   function generateWrapper(properties?: Partial<{showActions: boolean, showSection: boolean}>) {
    // Creating wrapper around MarketplaceCard with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { MarketplaceCard },
      template: `
      <div data-app>
        <MarketplaceCard :content="testMarketplaceCard" :showActions="showActions" :showSection="showSection"/>
      </div>`
    });

describe('HomePage.vue', () => {
  // Container for the HomePage under test
  let wrapper: Wrapper<any>;
  let store: Store<StoreData>;

  beforeEach(() => {
    const vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();

    wrapper = mount(HomePage, {
      stubs: ['BusinessActionPanel', 'UserActionPanel', 'GlobalMessage'],
      localVue,
      vuetify,
      store,
      data() {
        return {
          testMarketplaceCard: testMarketplaceCard,
          showActions: true,
          showSection: false,
          ...properties
        };
    }});
  });

  it('If no events are posted then there should be no events in the newsfeed', () => {
    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeFalsy();
  });

  it('If no events are posted then the message "No items in your feed" should be shown', () => {
    expect(wrapper.text()).toContain('No items in your feed');
  });

  it('If an message event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: AnyEvent = {
      type: 'MessageEvent',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      message: 'Hello world',
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeTruthy();
    expect(newsfeedItem.props().event).toBe(event);
    console.log(newsfeedItem.props());
  });

  it('If an expiry event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: AnyEvent = {
      type: 'ExpiryEvent',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      card: { MarketplaceCard },
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeTruthy();
    expect(newsfeedItem.props().event).toBe(event);
    console.log(newsfeedItem.props());
  });

  it('If an event is posted to the store then the message "No items in your feed" should not be shown', async () => {
    const event: AnyEvent = {
      type: 'MessageEvent',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      message: 'Hello world',
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    expect(wrapper.text()).not.toContain('No items in your feed');
  });
});