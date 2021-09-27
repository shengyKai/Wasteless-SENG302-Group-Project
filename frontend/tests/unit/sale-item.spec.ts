import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SaleItem from "@/components/cards/SaleItem.vue";
import FullProductDescription from "@/components/utils/FullProductDescription.vue";
import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      symbol: 'Currency symbol'
    };
  })
}));

describe('SaleItem.vue', () => {
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

    wrapper = mount(SaleItem, {
      localVue,
      vuetify,
      store,
      components: {
        FullProductDescription
      },
      stubs: {
        ImageCarousel: true,
        FullSaleListing: true,
      },
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
              "images": [
                {
                  "id": 1234,
                  "filename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg",
                  "thumbnailFilename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg"
                }
              ]
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
          "closes": "2021-07-21T23:59:00Z"
        },
        businessId: 1
      }
    });
    await wrapper.setData({
      currency: {
        code: "Currency code",
        symbol: "Currency symbol"
      }
    });
  });

  it("Must contain the product name and quantity", () => {
    expect(wrapper.text()).toContain("3 × Watties Baked Beans - 420g can");
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
          "images": [
            {
              "id": 1234,
              "filename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg",
              "thumbnailFilename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg"
            }
          ]
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
      "closes": "2021-07-21T23:59:00Z"
    }});
    await Vue.nextTick();
    let comp = wrapper.findComponent({ref:'sellerInfo'});
    expect(comp.exists()).toBeFalsy();
  });

  it("Contains the 'View more' button which reveals full product info", async ()=>{
    let button = wrapper.findComponent({ref:"viewMoreButton"});
    expect(button.exists()).toBeTruthy();
    expect(button.text()).toContain("View More");
    expect(wrapper.findComponent({name: 'FullSaleListing'}).exists()).toBeFalsy();

    button.trigger('click');
    await Vue.nextTick();

    expect(wrapper.findComponent({name: 'FullSaleListing'}).exists()).toBeTruthy();
  });

  it('When the Full sale listing is exitted it gets hidden', async () => {
    let button = wrapper.findComponent({ref:"viewMoreButton"});
    button.trigger('click');
    await Vue.nextTick();


    const fullSaleListing = wrapper.findComponent({name: 'FullSaleListing'});
    expect(fullSaleListing.exists()).toBeTruthy();

    fullSaleListing.vm.$emit('goBack');

    await Vue.nextTick();

    expect(wrapper.findComponent({name: 'FullSaleListing'}).exists()).toBeFalsy();
  });
});