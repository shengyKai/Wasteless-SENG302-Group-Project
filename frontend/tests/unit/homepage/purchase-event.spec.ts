import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import PurchaseEvent from '@/components/home/newsfeed/PurchasedEvent.vue';

import Vuex from 'vuex';


Vue.use(Vuetify);

describe('PurchasedEvent.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);

    wrapper = mount(PurchaseEvent, {
      localVue,
      vuetify,
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
                  "thumbnailFilename": "testImage.jpg",
                  "filename": "testImage.jpg",
                  "id": 2
                }],
                "created": "2021-09-20T02:17:58.234314Z"}, "images": [{
                "thumbnailFilename": "testImage.jpg",
                "filename": "testImage.jpg",
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
      stubs: ['router-link'],
    });
  });

  it("Title has quantity and name", () => {
    expect(wrapper.text()).toContain("Purchased 27x Humongous Vinegar");
  });

  it("Body has quantity and name", () => {
    expect(wrapper.text()).toContain("27x Humongous Vinegar");
  });

  it("Body has price", () => {
    expect(wrapper.text()).toContain("791.33");
  });

  it("Body has business name", () => {
    expect(wrapper.text()).toContain("Marywil Crescent Haberdashery");
  });

  it("Body has location", () => {
    expect(wrapper.text()).toContain("161 Clyde Road, Beaver County, Tapanui, Ulster, Malaysia");
  });
});