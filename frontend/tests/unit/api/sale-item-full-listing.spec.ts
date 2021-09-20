import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";
import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';
import { setListingInterest, getListingInterest} from '@/api/internal';

Vue.use(Vuetify);

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

    const app = document.createElement("div");
    app.setAttribute("data-app", "true");
    document.body.append(app);

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
      interestCount: "3",
    });
  });

  it.only("Must contain the product name and quantity", () => {
    expect(wrapper.findAllComponents({name:"v-card-text"})).toContain("3 × Watties Baked Beans - 420g can");
  });

  it("Must contain the sale price", () => {
    expect(wrapper.text()).toContain("Currency symbol17.99 Currency code");
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

  it("Must not contain seller info if seller info not present", async ()=>{
    await wrapper.setProps({saleItem:{
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
      "moreInfo": "",
      "created": "2021-07-14T11:44:00Z",
      "closes": "2021-07-21T23:59:00Z",
      "interestedCount": "5"
    }});
    await Vue.nextTick();
    let component = wrapper.findComponent({ref:'fullListing'});
    expect(component.exists()).toBeFalsy();
  });

  it("Must contain the description if present and under 50 chars", ()=>{
    expect(wrapper.text()).toContain("Baked Beans as they should be.");
  });
});