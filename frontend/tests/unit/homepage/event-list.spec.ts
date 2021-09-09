import Vue from "vue";
import Vuetify from "vuetify";
import {createLocalVue, mount, Wrapper} from "@vue/test-utils";
import EventList from "@/components/home/newsfeed/EventList.vue";
import Vuex from "vuex";
import {AnyEvent, GlobalMessageEvent} from "@/api/events";
import { version } from "vue/types/umd";

Vue.use(Vuetify);
const localVue = createLocalVue();

describe("EventList.vue", ()=> {
  let wrapper: Wrapper<any>;

  beforeEach(()=>{
    const vuetify = new Vuetify();
    localVue.use(Vuex);
    wrapper = mount(EventList, {
      stubs: ['GlobalMessage' , 'ExpiryEvent', 'DeleteEvent', 'KeywordCreated', "EventList"],
      localVue,
      vuetify,
      propsData: {
        isFiltered: false,
        events: [],
      }
    });
  });
  it('If no events are posted then there should be no events in the newsfeed', () => {
    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeFalsy();
  });

  it('If no events are posted then the message "No items in this feed" should be shown', () => {
    expect(wrapper.text()).toContain('No items in this feed');
  });


  it('If an global message event is provided then it should be displayed in the newsfeed', async () => {
    const event: AnyEvent = {
      type: 'GlobalMessageEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      message: 'Hello world',
      read: false,
      lastModified: new Date().toString(),
    };
    wrapper.setProps({
      events: [event]
    });
    await Vue.nextTick();

    let newsfeedItem = wrapper.findComponent({name: 'GlobalMessage'});
    expect(newsfeedItem.exists()).toBeTruthy();
    expect(newsfeedItem.props().event).toBe(event);
  });

  it('If an event is posted to the store then the message "No items in your feed" should not be shown', async () => {
    const event: AnyEvent = {
      type: 'GlobalMessageEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      created: new Date().toString(),
      message: 'Hello world',
      read: false,
      lastModified: new Date().toString()
    };
    wrapper.setProps({
      events: [event]
    });
    await Vue.nextTick();

    expect(wrapper.text()).not.toContain('No items in your feed');
  });

  it('If starred messages are shown then there should be a marker for it', async () => {
    const event1: GlobalMessageEvent = {
      type: 'GlobalMessageEvent',
      status: 'starred',
      id: 7,
      tag: 'none',
      read: false,
      created: new Date().toString(),
      message: 'Hello world',
      lastModified: new Date().toString(),
    };
    const event2: GlobalMessageEvent = {
      type: 'GlobalMessageEvent',
      status: 'starred',
      id: 8,
      tag: 'none',
      read: false,
      created: new Date().toString(),
      message: 'Hello world',
      lastModified: new Date().toString(),
    };
    wrapper.setProps({
      events: [event1, event2]
    });
    await Vue.nextTick();

    expect(wrapper.text()).toContain('Starred');
    expect(wrapper.text()).not.toContain('Unstarred');

    let events: (AnyEvent | string)[] = wrapper.vm.eventsPageWithSpacers;
    expect(events[0]).toBe('Starred'); // The marker should be first
    expect(events.filter(event => typeof event === 'string')).toStrictEqual(['Starred']); // There should only be one marker
  });

  it('If unstarred messages are shown then there should be a marker for it', async () => {
    const event1: GlobalMessageEvent = {
      type: 'GlobalMessageEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      read: false,
      created: new Date().toString(),
      message: 'Hello world',
      lastModified: new Date().toString(),
    };
    const event2: GlobalMessageEvent = {
      type: 'GlobalMessageEvent',
      status: 'normal',
      id: 8,
      tag: 'none',
      read: false,
      created: new Date().toString(),
      message: 'Hello world',
      lastModified: new Date().toString(),
    };
    wrapper.setProps({
      events: [event1, event2]
    });
    await Vue.nextTick();

    expect(wrapper.text()).toContain('Unstarred');
    expect(wrapper.text()).not.toContain('Starred');

    let events: (AnyEvent | string)[] = wrapper.vm.eventsPageWithSpacers;
    expect(events[0]).toBe('Unstarred'); // The marker should be first
    expect(events.filter(event => typeof event === 'string')).toStrictEqual(['Unstarred']); // There should only be one marker
  });
  it('If unstarred and starred messages are shown then there should be markers for both', async () => {
    const event1: GlobalMessageEvent = {
      type: 'GlobalMessageEvent',
      status: 'normal',
      id: 7,
      tag: 'none',
      read: false,
      created: new Date().toString(),
      message: 'Hello world',
      lastModified: new Date().toString(),
    };
    const event2: GlobalMessageEvent = {
      type: 'GlobalMessageEvent',
      status: 'starred',
      id: 8,
      tag: 'none',
      read: false,
      created: new Date().toString(),
      message: 'Hello world',
      lastModified: new Date().toString()
    };
    wrapper.setProps({
      events: [event2, event1]
    });
    await Vue.nextTick();

    expect(wrapper.text()).toContain('Unstarred');
    expect(wrapper.text()).toContain('Starred');

    let events: (AnyEvent | string)[] = wrapper.vm.eventsPageWithSpacers;
    expect(events[0]).toBe('Starred'); // The starred marker should be first
    expect(events[2]).toBe('Unstarred'); // The unstared marker should be after the starred marker and starred event
    expect(events.filter(event => typeof event === 'string')).toStrictEqual(['Starred', 'Unstarred']); // There should only be 2 markers
  });

  describe("Pagination the homefeed", () => {
    /**
     * Adds 12 events to the store. Half of them would be of a different colour from the other half.
     */
    async function addMultipleEvents() {
      let untagged = [];
      for (let i=0; i < 4; i++) {
        let event: AnyEvent = {
          type: 'GlobalMessageEvent',
          status: 'normal',
          id: i,
          tag: 'none',
          created: new Date().toString(),
          message: 'None event',
          read: false,
          lastModified: new Date().toString()
        };
        untagged.push(event);
      }
      let tagged = [];
      for (let i=4; i < 12; i++) {
        let event: AnyEvent = {
          type: 'GlobalMessageEvent',
          status: 'normal',
          id: i,
          tag: 'red',
          created: new Date().toString(),
          message: 'Red event',
          read: false,
          lastModified: new Date().toString()
        };
        tagged.push(event);
      }
      wrapper.setProps({
        events: [...untagged, ...tagged]
      });
      await Vue.nextTick();
    }

    beforeEach(async () => {
      await addMultipleEvents();
    });

    it('If there are more than 10 events present, pagination will be available for the user', async () => {
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
      await Vue.nextTick();
      events = wrapper.findAllComponents({ name: "GlobalMessage" });
      // Since there are 12 events total, the second page should only have 2 events
      expect(events.length).toEqual(2);

      // Check the results message
      expect(wrapper.text()).toContain("Displaying 11 - 12 of 12 results");
    });
  });
});


