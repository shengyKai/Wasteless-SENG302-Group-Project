
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import DeleteEvent from '@/components/home/newsfeed/DeleteEvent.vue';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @returns The generated user
 */
function makeTestUser(userId: number) {
  return {
    id:  userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: {
      streetNumber: 'test_street_number',
      streetName: 'test_street1',
      city: 'test_city',
      region: 'test_region',
      postcode: 'test_postcode',
      district: 'test_district',
      country: 'test_country' + userId
    },
    businessesAdministered: [],
  };
}

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

  describe('Card has been deleted from the databse', () => {
    it("Title contains delete message", () => {
      expect(wrapper.vm.title).toBe('Your marketplace card has been deleted');
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
})