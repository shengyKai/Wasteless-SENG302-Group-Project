import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import PurchaseEvent from '@/components/home/newsfeed/PurchasedEvent.vue';

import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { makeTestUser } from '../utils';
import router from "@/plugins/vue-router";

Vue.use(Vuetify);

describe('PurchasedEvent.vue', () => {
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
      router,
      propsData: {
        event: {
          "id": 1,
          "type": "PurchasedEvent",
          "created": "2021-09-20T02:18:04.939432Z",
          "tag": "none",
          "status": "normal",
          "lastModified": "2021-09-20T02:18:04.939432Z",
          "boughtSaleItem": {
            "id": 1,
            "buyer": {
              "id": 1,
              "firstName": "DGAA",
              "lastName": "DGAA",
              "email": "wasteless@seng302.com",
              "created": "2021-09-20T02:17:48.174051Z",
              "homeAddress": {
                "country": "wasteless",
                "city": "wasteless",
                "region": "wasteless"
              }
            },
            "product": {
              "id": "RWAJITCEY143230",
              "name": "Humongous Vinegar",
              "description": "Aliquid culpa officia aut! Impedit sit sunt quaerat, odit, tenetur error, harum nesciunt ipsum debitis quas aliquid. Reprehenderit, quia. Quo neque error r.",
              "manufacturer": "Clemons Corp",
              "recommendedRetailPrice": 224.77,
              "created": "2021-09-20T02:17:58.235311Z",
              "business": {
                "primaryAdministratorId": 2,
                "name": "Marywil Crescent Haberdashery",
                "description": "B laudantium modi minima sunt esse temporibus sint culpa, recusandae aliquam numquam totam ratione voluptas quod exercitationem fuga. Possimus quis earum veniam quasi aliquam eligendi,.",
                "address": {
                  "country": "Malaysia",
                  "city": "Tapanui",
                  "region": "Ulster",
                  "streetName": "Clyde Road",
                  "streetNumber": "161",
                  "postcode": "92500",
                  "district": "Beaver County"
                },
                "businessType": "Retail Trade",
                "id": 1,
                "images": [{
                  "thumbnailFilename": "/media/images/94c830e8-2c42-4c96-86e6-4db28a754793.jpg",
                  "filename": "/media/images/94c830e8-2c42-4c96-86e6-4db28a754793.jpg",
                  "id": 2
                }],
                "created": "2021-09-20T02:17:58.234314Z"}, "images": [{
                "thumbnailFilename": "/media/images/74f5e155-358b-4775-ae3b-abe981e721ef.jpg",
                "filename": "/media/images/74f5e155-358b-4775-ae3b-abe981e721ef.jpg",
                "id": 1
              }],
              "countryOfSale": "Malaysia"
            },
            "interestCount": 0,
            "price": 791.33,
            "quantity": 27,
            "saleDate": "2021-09-20T02:18:04.930481Z",
            "listingDate": "2021-07-27T18:00:12Z"
          },
          "read": false
        }
      },
    });
  });

  it("Title has quantity and name", () => {
    expect(wrapper.vm.title).toBe("Purchased 27x Humongous Vinegar");
  });

  it("Body has quantity and name", () => {
    expect(wrapper.vm.itemBought).toBe("27x Humongous Vinegar");
  });

  it("Body has price", () => {
    expect(wrapper.vm.price).toBe(791.33);
  });

  it("Body has business name", () => {
    expect(wrapper.vm.seller).toBe("Marywil Crescent Haberdashery");
  });

  it("Body has location", () => {
    expect(wrapper.vm.location).toBe("161 Clyde Road, Beaver County, Tapanui, Ulster, Malaysia");
  });
});