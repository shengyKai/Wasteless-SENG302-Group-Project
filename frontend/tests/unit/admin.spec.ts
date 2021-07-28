import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import Admin from "@/components/admin/Admin.vue";

import * as api from "@/api/internal";
import { castMock, flushQueue } from './utils';
import Vuex, { Store } from 'vuex';
import { getStore, resetStoreForTesting, StoreData } from '@/store';

Vue.use(Vuetify);

jest.mock('@/api/internal', () => ({
  searchKeywords: jest.fn(),
}));

const searchKeywords = castMock(api.searchKeywords);

describe("Admin.vue", () => {

  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  let store: Store<StoreData>;

  const keywordResponse : api.Keyword[] = [{
    id: 12,
    name: "Dance",
    created: "2021-01-01",
  }];

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);

    const app = document.createElement("div");
    app.setAttribute("data-app", "true");
    document.body.append(app);

    wrapper = mount(Admin, {
      localVue,
      vuetify,
      store,
    });
  });

  it('Calls searchKeywords with empty string', () => {
    expect(searchKeywords.mock.calls.length).toBe(1);
    expect(searchKeywords.mock.calls[0][0]).toBe("");
  });

  it('Sets keywords to response if API call returns an array of keywords', async () => {
    searchKeywords.mockResolvedValueOnce(keywordResponse);
    await wrapper.vm.setKeywords();
    expect(wrapper.vm.keywords).toStrictEqual(keywordResponse);
  });

  it('Sets keywords to undefined if API call returns an error message', async () => {
    searchKeywords.mockResolvedValueOnce("Error!");
    await wrapper.vm.setKeywords();
    expect(wrapper.vm.keywords).toBe(undefined);
  });

  it('Displays error message if API call returns an error message', async () => {
    searchKeywords.mockResolvedValueOnce("Error!");
    await wrapper.vm.setKeywords();
    expect(wrapper.vm.error).toBe("Error!");
  });

});