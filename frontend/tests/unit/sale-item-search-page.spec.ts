import { createLocalVue, shallowMount, Wrapper } from "@vue/test-utils";
import Vue from "vue";
import Vuex from 'vuex';
import Vuetify from "vuetify";
import router from '@/plugins/router';
import VueRouter from 'vue-router';

import SearchSaleItems from "@/components/SaleListing/SearchSaleItems.vue";
import { getStore, resetStoreForTesting } from "@/store";

Vue.use(Vuex);
resetStoreForTesting();

describe("SearchSaleItems.vue", () => {

  Vue.use(Vuetify);

  const localVue = createLocalVue();
  localVue.use(VueRouter);
  let wrapper : Wrapper<any>;

  beforeEach(() => {
    const vuetify = new Vuetify();
    const store = getStore();

    wrapper = shallowMount(SearchSaleItems, {
      localVue,
      vuetify,
      router,
      store,
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("If results is undefined, total results will be zero", async () => {
    await wrapper.setData({
      resultsPage: undefined,
    });
    expect(wrapper.vm.totalResults).toBe(0);
  });

  it("If results are present, total results will be taken from result count", async () => {
    await wrapper.setData({
      resultsPage: {
        count: 103
      },
    });
    expect(wrapper.vm.totalResults).toBe(103);
  });

  it("When an attribute of simpleSearchParams changes, simpleSearch will be called", async () => {
    wrapper.vm.simpleSearch = jest.fn();
    await wrapper.setData({
      simpleSearchParams: {
        reverse: true
      }
    });
    expect(wrapper.vm.simpleSearch).toBeCalledTimes(1);
  });

});


