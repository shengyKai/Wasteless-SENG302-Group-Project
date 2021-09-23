import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import SimpleSearchBar from "@/components/SaleListing/SimpleSearchBar.vue";

describe('AdvancedSearchBar.vue', () => {

  Vue.use(Vuetify);

  const localVue = createLocalVue();
  let wrapper : Wrapper<any>;
  let appWrapper : Wrapper<any>;

  let modelData : any = {};

  beforeEach(() => {
    const vuetify = new Vuetify();

    const App = localVue.component('App', {
      components: { SimpleSearchBar },
      template: '<div data-app><SimpleSearchBar v-model="modelData"/></div>',
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

    wrapper = appWrapper.getComponent(SimpleSearchBar);
  });

  afterEach(() => {
    wrapper.destroy();
    appWrapper.destroy();
  });

  it("showAdvancedSearch method emits showAdvancedSearch event", () => {
    wrapper.vm.showAdvancedSearch();
    expect(wrapper.emitted().showAdvancedSearch).toBeTruthy();
  });

  it("When searchParams of SimpleSearchBar are changed, object passed in through v-model prop is updated", async () => {
    const newSearchParams = {
      query: "Nathan Apple",
      orderBy: "Nathan",
      reverse: true
    };
    await wrapper.setData({
      searchParams: newSearchParams,
    });
    expect(modelData).toStrictEqual(newSearchParams);
  });

});