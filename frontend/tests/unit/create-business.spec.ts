import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import { createOptions } from '@/store';
import CreateBusiness from '@/components/BusinessProfile/CreateBusiness.vue';
//import LocationAutocomplete from '@/components/utils/LocationAutocomplete.vue';

Vue.use(Vuetify);

const localVue = createLocalVue();

localVue.use(VueRouter);
const router = new VueRouter();


describe('CreateBusiness.vue', () => {
  // Container for the CreateBusiness under test
  let wrapper: Wrapper<any>;

  /**
   * Sets up the test CreateBusiness instance
   */
  beforeEach(() => {
    localVue.use(Vuex);
    const store = new Vuex.Store(createOptions());
    const vuetify = new Vuetify();

    const App = localVue.component('App', {
      components: { CreateBusiness },
      template: '<div data-app><CreateBusiness/></div>',
    });

    const elem = document.createElement('div');
    document.body.appendChild(elem);

    const appWrapper = mount(App, {
      localVue,
      router,
      vuetify,
      store,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(CreateBusiness);
  });

  afterEach(() => {
    wrapper.destroy();
  });

  function populateRequiredFields() {
    wrapper.setData({
      business: 'Business Name',
      businessType: 'Business Type',
      street1: 'Street 1',
      city: 'City',
      state: 'State',
      country: 'Country',
      postcode: '1234',
    });
  }

  /**
   * Tests that the CreateBusiness is valid if all required fields are provided
   */
  it('Valid if all required fields are provided', async () => {
    populateRequiredFields();

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });
});
