import Vue from 'vue';
import Vuex, { Store } from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import HomePage from '@/components/home/HomePage.vue';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

import * as events from '@/api/events';
import {User} from "@/api/internal-user";
import {MarketplaceCard} from "@/api/internal-marketplace";
import {Keyword} from "@/api/internal-keyword";
import { castMock } from '../utils';

Vue.use(Vuetify);
const localVue = createLocalVue();

jest.mock('@/api/events', () => ({
  getEvents: jest.fn(),
}));

const getEvents = castMock(events.getEvents);

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

    jest.useFakeTimers();
    getEvents.mockResolvedValue([]);

    wrapper = mount(HomePage, {
      stubs: ['BusinessActionPanel', 'UserActionPanel', 'GlobalMessage' , 'ExpiryEvent', 'DeleteEvent', 'KeywordCreated'],
      localVue,
      vuetify,
      store,
    });
  });

  it('If an global message event is posted to the store then it should be contained in mainEvents', async () => {
    const event: events.AnyEvent = {
      type: 'GlobalMessageEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      message: 'Hello world',
      read: false,
      lastModified: new Date().toString()
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
    expect(mainEvents.exists()).toBeTruthy();
    expect(mainEvents.props().events).toStrictEqual([event]);
  });

  it('If an expiry event is posted to the store then it should be displayed in the mainEvents tab', async () => {
    const event: events.ExpiryEvent = {
      type: 'ExpiryEvent',
      status: 'normal',
      id: 2,
      tag: 'none',
      created: new Date().toString(),
      card: testMarketPlaceCard,
      read: false,
      lastModified: new Date().toString()
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
    expect(mainEvents.exists()).toBeTruthy();
    expect(mainEvents.props().events).toStrictEqual([event]);
  });

  it('If delete event is posted to the store then it should be displayed in the mainEvents tab', async () => {
    const event: events.DeleteEvent = {
      type: 'DeleteEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      title: "test_title",
      section: "ForSale",
      read: false,
      lastModified: new Date().toString()
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
    expect(mainEvents.exists()).toBeTruthy();
    expect(mainEvents.props().events).toStrictEqual([event]);
  });

  it('If an keyword created event is posted to the store then it should be displayed in the mainEvents tab', async () => {
    const event: events.KeywordCreatedEvent = {
      type: 'KeywordCreatedEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      keyword: {
        id: 1,
        name: "EDWARD",
        created: "2012/01/01"
      },
      creator: testUser,
      read: false,
      lastModified: new Date().toString()
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
    expect(mainEvents.exists()).toBeTruthy();
    expect(mainEvents.props().events).toStrictEqual([event]);
  });

  it('If an message event is posted to the store then it should be displayed in the mainEvents tab', async () => {
    const event: events.MessageEvent = {
      type: 'MessageEvent',
      status: 'normal',
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
      read: false,
      lastModified: new Date().toString()
    };
    store.commit('addEvent', event);
    await Vue.nextTick();

    let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
    expect(mainEvents.exists()).toBeTruthy();
    expect(mainEvents.props().events).toStrictEqual([event]);
  });

  describe('Event statuses', () => {
    it('Starred messages are shown in the main newsfeed', async () => {
      const event: events.GlobalMessageEvent = {
        type: 'GlobalMessageEvent',
        status: 'starred',
        id: 7,
        tag: 'none',
        read: false,
        created: new Date().toString(),
        message: 'Hello world',
        lastModified: new Date().toString(),
      };
      store.commit('addEvent', event);
      await Vue.nextTick();

      let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
      expect(mainEvents.exists()).toBeTruthy();
      expect(mainEvents.props().events).toStrictEqual([event]);
    });

    it('Archived messages are not shown in the main newsfeed and are shown in archived tab', async () => {
      const event: events.GlobalMessageEvent = {
        type: 'GlobalMessageEvent',
        status: 'archived',
        id: 7,
        tag: 'none',
        read: false,
        created: new Date().toString(),
        message: 'Hello world',
        lastModified: new Date().toString(),
      };
      store.commit('addEvent', event);
      await Vue.nextTick();

      let mainEvents = wrapper.findComponent({ref: 'mainEvents'});
      expect(mainEvents.exists()).toBeTruthy();
      expect(mainEvents.props().events).not.toStrictEqual([event]);

      let archivedEvents = wrapper.findComponent({ref: 'archivedEvents'});
      expect(archivedEvents.exists()).toBeTruthy();
      expect(archivedEvents.props().events).toStrictEqual([event]);
    });
  });

  describe("filtering of the homefeed", () => {
    /**
     * Adds 12 events to the store. Half of them would be of a different colour from the other half.
     */
    async function addMultipleEvents() {
      for (let i=0; i < 4; i++) {
        let event: events.AnyEvent = {
          type: 'GlobalMessageEvent',
          status: 'normal',
          id: i,
          tag: 'none',
          created: new Date().toString(),
          message: 'None event',
          read: false,
          lastModified: new Date().toString()
        };
        store.commit('addEvent', event);
        await Vue.nextTick();
      }
      for (let i=4; i < 12; i++) {
        let event: events.AnyEvent = {
          type: 'GlobalMessageEvent',
          status: 'normal',
          id: i,
          tag: 'red',
          created: new Date().toString(),
          message: 'Red event',
          read: false,
          lastModified: new Date().toString()
        };
        store.commit('addEvent', event);
        await Vue.nextTick();
      }
    }

    beforeEach(async () => {
      await addMultipleEvents();
    });

    it('If there are events with different tag colours, the user can filter them to the colour of interest and see the correct results', async () => {
      await wrapper.setData({
        filterBy: ["red"]
      });

      let mainEvents = wrapper.findComponent({ref:"mainEvents"});
      let events = mainEvents.vm.$props.events;
      // Number of events should be 8 because there are only 8 red tagged events
      expect(events.length).toEqual(8);

      await wrapper.setData({
        filterBy: ["none"]
      });

      events = mainEvents.vm.$props.events;
      // Number of events should be 4 because there are only 4 none tagged events
      expect(events.length).toEqual(4);
    });

    it('If there are events with different tag colours, the user can filter them with multiple colours and see the correct results', async () => {
      await wrapper.setData({
        filterBy: ["red", "none"]
      });

      let mainEvents = wrapper.findComponent({ref:"mainEvents"});
      let events = mainEvents.vm.$props.events;
      // Number of events should be 10 because there are 12 red and none tagged events total and 10 are showed in the first page
      expect(events.length).toEqual(12);
    });

    it('If the user filters with tags which are not in the events, no results will be shown', async () => {
      await wrapper.setData({
        filterBy: ["blue", "purple"]
      });
      let mainEvents = wrapper.findComponent({ref:"mainEvents"});
      let events = mainEvents.vm.$props.events;
      expect(events.length).toEqual(0);
    });
  });
});