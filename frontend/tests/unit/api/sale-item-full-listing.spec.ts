import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";
import Vuex, { Store } from 'vuex';
import * as api from '@/api/internal';
import { User } from '@/api/internal';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import {createSaleItem} from '@/api/internal';
import { castMock } from '../utils';

Vue.use(Vuetify);

const testUser: User = {
  id: 2,
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  email: 'test_email',
  homeAddress: { country: 'test_country', city: 'test_city', district: 'test_district'},
};

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      symbol: 'Currency symbol'
    };
  })
}));

jest.mock('@/api/internal', () => ({
  getListingInterest: jest.fn(),
  setListingInterest: jest.fn(),
}));

const getListingInterest = castMock(api.getListingInterest);
const setListingInterest = castMock(api.getListingInterest);

describe('FullSaleListing.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  let store: Store<StoreData>;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = testUser;

    wrapper = mount(FullSaleListing, {
      localVue,
      vuetify,
      store,
      propsData: {
        saleItem: {
          "id": 57,
          "inventoryItem": {
            "id": 101,
            "product": {
              "id": "WATT-420-BEANS",
              "name": "Watties Baked Beans - 420g can",
              "description": "Baked Beans as they should be.",
              "manufacturer": "Heinz Wattie's Limited",
              "recommendedRetailPrice": 2.2,
              "created": "2021-05-15T05:55:32.808Z",
              "countryOfSale": "New Zealand",
              "images": [],
            },
            "quantity": 4,
            "pricePerItem": 6.5,
            "totalPrice": 21.99,
            "manufactured": "2021-05-15",
            "sellBy": "2021-05-15",
            "bestBefore": "2021-05-15",
            "expires": "2021-05-15"
          },
          "quantity": 3,
          "price": 17.99,
          "moreInfo": "Seller may be willing to consider near offers.",
          "created": "2021-07-14T11:44:00Z",
          "closes": "2021-07-21T23:59:00Z",
          "interestedCount": "5"
        },
        businessId: 1
      }
    });

    await wrapper.setData({
      currency: {
        code: "Currency code",
        symbol: "Currency symbol"
      },
      interestCount: "1",
    });
  });

  it("Must contain the product name and quantity", () => {
    expect(wrapper.text()).toContain("Watties Baked Beans - 420g can");
  });

  it("Must contain the Total price", () => {
    expect(wrapper.text()).toContain("17.99");
  });

  it("Must contain the listing quantity", () => {
    expect(wrapper.text()).toContain("3");
  });

  it("Must contain a formatted created date", () => {
    expect(wrapper.text()).toContain("14 Jul 2021");
  });

  it("Must contain a formatted expiry date", () => {
    expect(wrapper.text()).toContain("15 May 2021");
  });

  it("Must contain a formatted close date", () => {
    expect(wrapper.text()).toContain("22 Jul 2021");
  });

  it("Must contain seller info if seller info present and under 50 chars", () => {
    let component = wrapper.findComponent({ref:'fullListing'});
    expect(component.exists()).toBeTruthy();
    expect(wrapper.text()).toContain("Seller may be willing to consider near offers.");
  });

  it("Must contain the description if present and under 50 chars", ()=>{
    expect(wrapper.text()).toContain("Baked Beans as they should be.");
  });

  it("Must contain the manufactured origin country of the sale", () => {
    expect(wrapper.text()).toContain("Country: New Zealand");
  });

  it("Must contain the manufactured details", () => {
    expect(wrapper.text()).toContain("Manufacturer: Heinz Wattie's Limited Original");
  });

  it("Must contain interested like count for the listing", () => {
    expect(wrapper.text()).toContain("Like 1");
  });

  it("Update user status upon clicking on the like button", async () => {
    const likeButton = wrapper.findComponent({ref:'likeButton'});
    expect(likeButton.exists()).toBeTruthy;
    await likeButton.trigger('click');
    await Vue.nextTick();
    expect(setListingInterest).toBeCalled();
    expect(getListingInterest).toBeCalled();
  });

});