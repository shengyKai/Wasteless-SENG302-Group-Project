import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import CreateProduct from '@/components/BusinessProfile/CreateProduct.vue';

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('CreateProduct.vue', () => {
  // Container for the wrapper around CreateProduct
  let appWrapper: Wrapper<any>;

  // Container for the CreateProduct under test
  let wrapper: Wrapper<any>;


  /**
   * Sets up the test CreateProduct instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around CreateProduct with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { CreateProduct },
      template: '<div data-app><CreateProduct/></div>',
    });

    // Put the CreateProduct component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(CreateProduct);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the CreateProduct component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Adds all the fields that are required for the create business form to be valid
   *
   * These are:
   * - Product name
   * - Product shortcode
   */
  async function populateRequiredFields() {
    await wrapper.setData({
      product: 'Product Name',
      productCode: 'ABC-XYZ-0123456789',
    });
  }

  async function populateAllFields() {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'Product Description',
      manufacturer: 'Product Manufacturer',
      expiryDate: '1/1/1900',
      recommendedRetailPrice: '100.00',
      quantity: '50',
    });
  }

  /**
   * Tests that the CreateProduct is valid if all required fields are provided
   */
  it('Valid if all required fields are provided', async () => {
    await populateRequiredFields();

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });

  /**
   * Tests that the CreateProduct is valid if all fields are provided (include the non-essential)
   */
  it('Valid if all fields are provided', async () => {
    await populateAllFields();

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });

  /**
   * Tests that the CreateProduct is invalid if product name is not provided
   */
  it('Invalid if no product name', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      product: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product name is too long (> 100 characters)
   */
  it('Invalid if product name is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      product: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product description is too long (> 200 characters)
   */
  it('Invalid if description is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'a'.repeat(201),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product manufacturer is too long (> 100 characters)
   */
  it('Invalid if manufacturer is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      manufacturer: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product price is not a number
   */
  it('Invalid if product price is not a number', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: 'not a number',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product price is a negative number
   */
  it('Invalid if product price is negative', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '-10',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product price is too high
   */
  it('Invalid if product price is too high', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '100001',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product price has too many decimal digits
   */
  it('Invalid if product price has too many digits after the decimal', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '3.141',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product price has a single decimal digit
   */
  it('Invalid if product price has a single decimal digit', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '3.1',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product quantity is negative
   */
  it('Invalid if quantity is negative', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      quantity: '-1',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product quantity is too large
   */
  it('Invalid if quantity is too large', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      quantity: '100001',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product quantity is not a number
   */
  it('Invalid if quantity is not a number', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      quantity: 'not really a number',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if the product quantity is not a integer
   */
  it('Invalid if quantity is not a integer', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      quantity: '3.141592653589',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if no product code is provided
   */
  it('Invalid if no product code is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if product code uses lowercase letters
   */
  it('Invalid if product code uses lowercase letters', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: 'aaaaaaaaa',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateProduct is invalid if product code uses spaces
   */
  it('Invalid if product code has spaces', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: '          ',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });
});
