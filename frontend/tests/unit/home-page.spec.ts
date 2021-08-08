import Vue from 'vue';
import Vuex, { Store } from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import HomePage from '@/components/home/HomePage.vue';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

import { AnyEvent } from '@/api/events';

Vue.use(Vuetify);
const localVue = createLocalVue();


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
    });
  });

  it('If no events are posted then there should be no events in the newsfeed', () => {
    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeFalsy();
  });

  it('If no events are posted then the message "No items in your feed" should be shown', () => {
    expect(wrapper.text()).toContain('No items in your feed');
  });

  it('If an event is posted to the store then it should be displayed in the newsfeed', async () => {
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