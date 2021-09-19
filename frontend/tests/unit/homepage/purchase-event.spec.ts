import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import PurchaseEvent from '@/components/home/newsfeed/PurchaseEvent.vue';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { makeTestUser } from '../utils';

Vue.use(Vuetify);

describe('PurchaseEvent.vue', () => {
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

    const app = document.createElement("div");
    app.setAttribute("data-app", "true");
    document.body.append(app);

    wrapper = mount(PurchaseEvent, {
      localVue,
      vuetify,
      store,
      propsData: {
        event: {
          saleItem: {
            id: 1,
            inventoryItem: {
              id: 1,
              product: {
                id: 1,
                name: "Apples",
                images: [],
                business: {
                  id: 1,
                  primaryAdministratorId: 1,
                  name: "The Orchard",
                  address: {
                    streetNumber: 23,
                    streetName: "Here St",
                    city: "Appledom",
                    country: "New Zealand"
                  },
                  businessType: "Retail Trade"
                },
              },
              quantity: 12,
              remainingQuantity: 9,
              expires: "2021-11-20"
            },
            quantity: 3,
            price: 2.50,
            created: "2021-09-18",
            interestCount: 5
          }
        }
      },
    });
  });
});