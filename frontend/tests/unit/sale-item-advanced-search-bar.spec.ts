import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import AdvancedSearchBar from "@/components/SaleListing/AdvancedSearchBar.vue";

jest.mock('@/api/internal', () => ({
  BUSINESS_TYPES: ['Type 1', 'Type 2', 'Type 3'],
}));

describe('AdvancedSearchBar.vue', () => {


  Vue.use(Vuetify);

  const localVue = createLocalVue();
  let wrapper : Wrapper<any>;
  let appWrapper : Wrapper<any>;

  let modelData : any = {};

  const validPrices = [undefined,"1","999","123765","23546.00","888888.12"];
  const invalidPrices = ["ABC", "-3", "!", "1.2", " ", "1000001", "3.1415", "4A"];

  beforeEach(() => {
    const vuetify = new Vuetify();

    const App = localVue.component('App', {
      components: { AdvancedSearchBar },
      template: '<div data-app><AdvancedSearchBar v-model="modelData"/></div>',
      data() {
        return {
          modelData: modelData,
        };
      }
    });

    appWrapper = mount(App, {
      localVue,
      vuetify,
    });

    wrapper = appWrapper.getComponent(AdvancedSearchBar);
  });

  afterEach(() => {
    wrapper.destroy();
    appWrapper.destroy();
  });

  it("hideAdvancedSearch method emits hideAdvancedSearch event", () => {
    wrapper.vm.hideAdvancedSearch();
    expect(wrapper.emitted().hideAdvancedSearch).toBeTruthy();
  });

  it("searchListings method emits searchListings event", () => {
    wrapper.vm.searchListings();
    expect(wrapper.emitted().searchListings).toBeTruthy();
  });

  it("businessTypeOptions generates a list of text-value pairs for all business types plus an 'Any' option", () => {
    const expectedOptions = [
      {text: "Any", value: undefined},
      {text: "Type 1", value: "Type 1"},
      {text: "Type 2", value: "Type 2"},
      {text: "Type 3", value: "Type 3"},
    ];
    const actualOptions = wrapper.vm.businessTypeOptions;
    expect(actualOptions).toStrictEqual(expectedOptions);
  });

  describe("No lowest price entered", () => {

    beforeEach(async () => {
      await wrapper.setData({
        searchParams: {
          lowestPrice: undefined
        }
      });
      expect(wrapper.vm.searchParams.lowestPrice).toBe(undefined);
    });

    it.each(validPrices)(
      "Highest price %s is valid as it is empty or a number less than 1,000,000 with 0 or 2 numbers after the decimal point",
      async (price) => {
        await wrapper.setData({
          searchParams: {
            highestPrice: price
          }
        });
        expect(wrapper.vm.highestPriceValid).toBeTruthy();
      }
    );

    it.each(invalidPrices)(
      "Highest price %s is invalid as it is not empty or a number less than 1,000,000 with 0 or 2 numbers after the decimal point",
      async (price) => {
        await wrapper.setData({
          searchParams: {
            highestPrice: price
          }
        });
        expect(wrapper.vm.highestPriceValid).toBeFalsy;
      }
    );
  });

  describe("No highest price entered", () => {

    beforeEach(async () => {
      await wrapper.setData({
        searchParams: {
          highestPrice: undefined
        }
      });
      expect(wrapper.vm.searchParams.highestPrice).toBe(undefined);
    });

    it.each(validPrices)(
      "Lowest price %s is valid as it is empty or a number less than 1,000,000 with 0 or 2 numbers after the decimal point",
      async (price) => {
        await wrapper.setData({
          searchParams: {
            lowestPrice: price
          }
        });
        expect(wrapper.vm.lowestPriceValid).toBeTruthy();
      }
    );

    it.each(invalidPrices)(
      "Lowest price %s is invalid as it is not empty or a number less than 1,000,000 with 0 or 2 numbers after the decimal point",
      async (price) => {
        await wrapper.setData({
          searchParams: {
            lowestPrice: price
          }
        });
        expect(wrapper.vm.lowestPriceValid).toBeFalsy;
      }
    );
  });

  it("Lowest and highest price are both invalid if highest price is lower than lowest price", async () => {
    await wrapper.setData({
      searchParams: {
        lowestPrice: "10",
        highestPrice: "9"
      }
    });
    expect(wrapper.vm.lowestPriceValid).toBeFalsy();
    expect(wrapper.vm.highestPriceValid).toBeFalsy();
  });

  it("Lowest and highest price are both invalid if highest price is equal to lowest price", async () => {
    await wrapper.setData({
      searchParams: {
        lowestPrice: "10",
        highestPrice: "10"
      }
    });
    expect(wrapper.vm.lowestPriceValid).toBeFalsy();
    expect(wrapper.vm.highestPriceValid).toBeFalsy();
  });

  it("Lowest and highest price are both valid if highest price is higher than lowest price", async () => {
    await wrapper.setData({
      searchParams: {
        lowestPrice: "9",
        highestPrice: "10"
      }
    });
    expect(wrapper.vm.lowestPriceValid).toBeTruthy();
    expect(wrapper.vm.highestPriceValid).toBeTruthy();
  });

  it("When searchParams of AdvancedSearchBar are changed, object passed in through v-model prop is updated", async () => {
    const newSearchParams = {
      productQuery: "Nathan Apple",
      businessQuery: "Nathan Apple LTD",
      locationQuery: "Nathan's house",
      closesBefore: "2021-12-31",
      closesAfter: "2021-01-01",
      orderBy: "Nathan",
      businessType: "Nathan",
      lowestPrice: "100",
      highestPrice: "200",
      reverse: true
    };
    await wrapper.setData({
      searchParams: newSearchParams,
    });
    expect(modelData).toStrictEqual(newSearchParams);
  });

  it("Search button disabled if lowest price invalid", async () => {
    await wrapper.setData({
      searchParams: {
        lowestPrice: "A",
      }
    });
    expect(wrapper.vm.lowestPriceValid).toBeFalsy();
    expect(wrapper.vm.searchButtonDisabled).toBeTruthy();
  });

  it("Search button disabled if highest price invalid", async () => {
    await wrapper.setData({
      searchParams: {
        highestPrice: "A",
      }
    });
    expect(wrapper.vm.highestPriceValid).toBeFalsy();
    expect(wrapper.vm.searchButtonDisabled).toBeTruthy();
  });

  it("Search button enabled if lowest and highest price both valid", async () => {
    await wrapper.setData({
      searchParams: {
        lowestPrice: undefined,
        highestPrice: undefined,
      }
    });
    expect(wrapper.vm.lowestPriceValid).toBeTruthy();
    expect(wrapper.vm.highestPriceValid).toBeTruthy();
    expect(wrapper.vm.searchButtonDisabled).toBeFalsy();
  });

});