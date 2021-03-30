import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import { createOptions } from '@/store';
import CreateBusiness from '@/components/BusinessProfile/CreateBusiness.vue';

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

    // Creating wrapper around CreateBusiness with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { CreateBusiness },
      template: '<div data-app><CreateBusiness/></div>',
    });

    // Put the CreateBusiness component inside a div in the global document,
    // this seems to make vuetify work correctly
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

    // The jsdom test runner doesn't declare the fetch function,
    // hence we need to implement it ourselves to make LocationAutocomplete not crash
    globalThis.fetch = async () => {
      return {
        json() {
          return {
            features: [],
          };
        }
      } as any;
    };
  });

  afterEach(() => {
    // Makes sure that the CreateBusiness component is removed from the global document
    wrapper.destroy();
  });

  /**
   * Adds all the fields that are required for the create business form to be valid
   *
   * These are:
   * - Business name
   * - Business type
   * - Street address line 1
   * - City
   * - State
   * - Country
   * - Postcode
   */
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



  /**
   * Tests that the CreateBusiness is invalid if description field is too long (> 200 characters)
   */
  it('Invalid if description too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      description: 'a'.repeat(201),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no business name field is provided
   */
  it('Invalid if business name not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      business: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if business name field is too long (> 100 characters)
   */
  it('Invalid if business name too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      business: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no business type field is provided
   */
  it('Invalid if business type not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      businessType: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no street line 1 field is provided
   */
  it('Invalid if street line 1 not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      street1: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if district field is too long (> 100 characters)
   */
  it('Invalid if district too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      district: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no city field is provided
   */
  it('Invalid if city not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      city: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if city field is too long (> 100 characters)
   */
  it('Invalid if city too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      city: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no state field is provided
   */
  it('Invalid if state not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      state: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if state field is too long (> 100 characters)
   */
  it('Invalid if state too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      state: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no country field is provided
   */
  it('Invalid if country not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      country: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if country field is too long (> 100 characters)
   */
  it('Invalid if country too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      country: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no postcode field is provided
   */
  it('Invalid if postcode not provided', async () => {
    populateRequiredFields();
    wrapper.setData({
      postcode: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if postcode field is too long (> 100 characters)
   */
  it('Invalid if postcode too long', async () => {
    populateRequiredFields();
    wrapper.setData({
      postcode: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });
});
