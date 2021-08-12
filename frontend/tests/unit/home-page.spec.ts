import Vue from 'vue';
import Vuex, { Store } from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import {User, MarketplaceCard, Keyword } from '@/api/internal';
import HomePage from '@/components/home/HomePage.vue';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

import { AnyEvent, DeleteEvent, ExpiryEvent, KeywordCreatedEvent } from '@/api/events';

Vue.use(Vuetify);
const localVue = createLocalVue();

const testUser: User = {
  id: 2,
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  email: 'test_email',
  homeAddress: { country: 'test_country', city: 'test_city', district: 'test_district'},
};

const testKeyword: Keyword = {
  id: 1,
  name: "Edward",
  created: "01/01/2020"
};

const testMarketPlaceCard: MarketplaceCard = {
  id: 1,
  creator: testUser,
  section: 'ForSale',
  created: "01/01/2020",
  lastRenewed: "02/01/2020",
  displayPeriodEnd: "05/01/2020",
  title: "test_title",
  description: "test_desription",
  keywords: [testKeyword]
};

describe('HomePage.vue', () => {
  // Container for the HomePage under test
  let wrapper: Wrapper<any>;
  let store: Store<StoreData>;

  beforeEach(() => {
    const vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = testUser;

    wrapper = mount(HomePage, {
      stubs: ['BusinessActionPanel', 'UserActionPanel', 'GlobalMessage' , 'ExpiryEvent', 'DeleteEvent', 'KeywordCreated'],
      localVue,
      vuetify,
      store,
    });
  });

  it('If no events are posted then there should be no events in the newsfeed', () => {
    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeFalsy();
  });

  it('If no events are posted then the message "No items in your feed" should be shown', () => {
    expect(wrapper.text()).toContain('No items in your feed');
  });

  it('If an global message event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: AnyEvent = {
      type: 'GlobalMessageEvent',
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
  });

  it('If an expiry event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: ExpiryEvent = {
      type: 'ExpiryEvent',
      id: 2,
      tag: 'none',
      created: new Date().toString(),
      card: testMarketPlaceCard,
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'ExpiryEvent'});
    expect(newsfeedItem.exists()).toBeTruthy();
    expect(newsfeedItem.props().event).toBe(event);
  });

  it('If delete event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: DeleteEvent = {
      type: 'DeleteEvent',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      title: "test_title",
      section: "ForSale"
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'DeleteEvent'});
    expect(newsfeedItem.exists()).toBeTruthy();
    expect(newsfeedItem.props().event).toBe(event);
  });

  it('If an keyword created event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: KeywordCreatedEvent = {
      type: 'KeywordCreatedEvent',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      keyword: {
        id: 1,
        name: "EDWARD",
        created: "2012/01/01"
      },
      creator: testUser
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'KeywordCreated'});
    expect(newsfeedItem.exists()).toBeTruthy();
    expect(newsfeedItem.props().event).toBe(event);
  });

  it('If an event is posted to the store then the message "No items in your feed" should not be shown', async () => {
    const event: AnyEvent = {
      type: 'GlobalMessageEvent',
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