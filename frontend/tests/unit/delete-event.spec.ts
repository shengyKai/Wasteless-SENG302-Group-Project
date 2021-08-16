
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import DeleteEvent from '@/components/home/newsfeed/DeleteEvent.vue';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { makeTestUser } from './utils';

Vue.use(Vuetify);

describe('DeleteEvent.vue', () => {
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

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append (app);

    wrapper = mount(DeleteEvent, {
      localVue,
      vuetify,
      store,
      propsData: {
        event: {
          id: 101,
          title: "Card title",
          section: "ForSale",
          created: "2021-01-01T12:00:00Z",
          type: "DeleteEvent"
        },
      }
    });
  });

  describe('Card has been deleted from the database', () => {
    it("Title contains delete message", () => {
      let eventWrapper = wrapper.findComponent({ name: 'Event'});
      expect(eventWrapper.props().title).toBe('Your marketplace card has been deleted');
    });

    it("Text contains delete message if the card is in the ForSale section ", () => {
      expect(wrapper.vm.text).toBe('Your marketplace card "Card title" in the For Sale section has been removed from the marketplace');
    });

    it("Text contains delete message if the card is in other sections other than ForSale", async () => {
      await wrapper.setProps({
        event: {
          id: 101,
          title: "Card title",
          section: "Wanted",
          created: "2021-01-01T12:00:00Z",
          type: "DeleteEvent"
        },
      });
      expect(wrapper.vm.text).toBe('Your marketplace card "Card title" in the Wanted section has been removed from the marketplace');
    });

    it('Must match snapshot', () => {
      expect(wrapper).toMatchSnapshot();
    });
  });
});