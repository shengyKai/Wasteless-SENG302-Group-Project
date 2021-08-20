import Vue from 'vue';
import Vuex, { Store } from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import {User, MarketplaceCard, Keyword } from '@/api/internal';
import HomePage from '@/components/home/HomePage.vue';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

import { AnyEvent, DeleteEvent, ExpiryEvent, KeywordCreatedEvent, MessageEvent } from '@/api/events';

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

  it('If an message event is posted to the store then it should be displayed in the newsfeed', async () => {
    const event: MessageEvent = {
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
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'MessageEvent'});
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

  describe("Pagination and filtering of the homefeed", () => {
    /**
     * Adds 12 events to the store. Half of them would be of a different colour from the other half.
     */
    async function addMultipleEvents() {
      for (let i=0; i < 4; i++) {
        let event: AnyEvent = {
          type: 'GlobalMessageEvent',
          id: i,
          tag: 'none',
          created: new Date().toString(),
          message: 'None event',
        };
        store.commit('addEvent', event);
        await Vue.nextTick();
      }
      for (let i=4; i < 12; i++) {
        let event: AnyEvent = {
          type: 'GlobalMessageEvent',
          id: i,
          tag: 'red',
          created: new Date().toString(),
          message: 'Red event',
        };
        store.commit('addEvent', event);
        await Vue.nextTick();
      }
    }

    beforeEach(async () => {
      await addMultipleEvents();
    });

    it('If there are more than 10 events in the store for the user, pagination will be available for the user', async () => {
      expect(wrapper.vm.totalPages).toEqual(2);
    });

    it('If there are more than 10 events in the store for the user, the user can navigate to the second page and see the correct results', async () => {
      let events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Number of events should be 10 because each page only allows space for 10 events
      expect(events.length).toEqual(10);
      // Check the results message
      expect(wrapper.text()).toContain("Displaying 1 - 10 of 12 results");

      // Set the page to 2
      await wrapper.setData({
        currentPage: 2
      });
      events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Since there are 12 events total, the second page should only have 2 events
      expect(events.length).toEqual(2);

      // Check the results message
      expect(wrapper.text()).toContain("Displaying 11 - 12 of 12 results");
    });

    it('If there are events with different tag colours, the user can filter them to the colour of interest and see the correct results', async () => {
      await wrapper.setData({
        filterBy: ["red"]
      });

      let events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Number of events should be 8 because there are only 8 red tagged events
      expect(events.length).toEqual(8);

      await wrapper.setData({
        filterBy: ["none"]
      });

      events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Number of events should be 4 because there are only 4 none tagged events
      expect(events.length).toEqual(4);
    });

    it('If there are events with different tag colours, the user can filter them with multiple colours and see the correct results', async () => {
      await wrapper.setData({
        filterBy: ["red", "none"]
      });

      let events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Number of events should be 10 because there are 12 red and none tagged events total and 10 are showed in the first page
      expect(events.length).toEqual(10);

      // Set the page to 2
      await wrapper.setData({
        currentPage: 2
      });
      events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Number of events should be 2 because there are 12 red and none tagged events total and 2 are showed in the second page
      expect(events.length).toEqual(2);
    });

    it('If the user filters with tags which are not in the events, no results will be shown', async () => {
      await wrapper.setData({
        filterBy: ["blue", "purple"]
      });

      let events = wrapper.findAllComponents({ name: "GlobalMessage" });
      expect(events.length).toEqual(0);
      expect(wrapper.text()).toContain("No items matches the filter");
    });
  });
});