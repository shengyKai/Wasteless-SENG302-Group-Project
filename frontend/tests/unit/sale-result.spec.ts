import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SaleResult from "@/components/SaleListing/SaleResult.vue";

Vue.use(Vuetify);

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      symbol: 'Currency symbol'
    };
  })
}));

describe('SaleResult.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    jest.clearAllMocks();
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    wrapper = mount(SaleResult, {
      localVue,
      vuetify,
      stubs: ['FullSaleListing', 'router-link'],
      propsData: {
        saleItem: {
          "id": 57,
          "inventoryItem": {
            "id": 101,
            "product": {
              "business": {
                "id": 5,
                "name": "This Inc.",
              },
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
  });

  it('FullSaleListing should not initial be shown', async () => {
    expect(wrapper.findComponent({name: 'FullSaleListing'}).exists()).toBeFalsy();
  });

  describe('Title clicked', () => {
    beforeEach(async () => {
      const title = wrapper.findComponent({ref: 'title'});
      await title.trigger('click');
      await Vue.nextTick();

    });

    it('FullSaleListing should be shown after title is clicked', () => {
      expect(wrapper.findComponent({name: 'FullSaleListing'}).exists()).toBeTruthy();
    });

    it('When "goBack" is emitted by FullSaleListing the FullSaleListing should be hidden', async () => {
      wrapper.findComponent({name: 'FullSaleListing'}).vm.$emit('goBack');
      await Vue.nextTick();

      expect(wrapper.findComponent({name: 'FullSaleListing'}).exists()).toBeFalsy();
    });

    it('When "refresh" is emitted by FullSaleListing the event should be passed on', async () => {
      expect(wrapper.emitted().refresh).toBeFalsy();

      wrapper.findComponent({name: 'FullSaleListing'}).vm.$emit('refresh');
      await Vue.nextTick();

      expect(wrapper.emitted().refresh).toBeTruthy();
    });
  });

  it('Component matches snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });
});