import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import CreateProduct from '@/components/BusinessProfile/CreateProduct.vue';
import { castMock, flushQueue } from './utils';
import * as api from '@/api';
import { getStore, resetStoreForTesting } from '@/store';
import { currencyFromCountry } from '@/components/utils/Methods/currency';

jest.mock('@/api', () => ({
  createProduct: jest.fn(),
  getBusiness: jest.fn(() => {
    return {
      address: {
        country: 'New Zealand',
      }
    }
  })
}));

jest.mock('@/components/utils/Methods/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      symbol: 'Currency symbol'
    }
  })
}));


const createProduct = castMock(api.createProduct);

Vue.use(Vuetify);

// Characters that are in the set of letters, numbers, spaces and punctuation.
const validCharacters = [" ", ":", ",", "7", "é", "树", "A"];
// Characters that are not a letter, number, space or punctuation.
const invalidCharacters = ["\uD83D\uDE02", "\uFFFF"];
// Characters that are whitespace not including the space character.
const whitespaceCharacters = ["\n", "\t"];

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
    localVue.use(Vuex);
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

    resetStoreForTesting();
    let store = getStore();
    store.state.createProductDialogBusiness = 90;

    appWrapper = mount(App, {
      localVue,
      vuetify,
      store: store,
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
      productCode: 'ABC-XYZ-012-789',
    });
  }

  /**
   * Populates all fields of the CreateProduct form
   *
   * Which include the product's:
   *  - Name
   *  - Shortcode
   *  - Description
   *  - Manufacturer
   *  - Recommended retail price
   */
  async function populateAllFields() {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'Product Description',
      manufacturer: 'Product Manufacturer',
      recommendedRetailPrice: '100.00',
    });
  }

  /**
   * Finds the close button in the CreateProduct form
   *
   * @returns A Wrapper around the close button
   */
  function findCloseButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Close'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the create button in the CreateProduct form
   *
   * @returns A Wrapper around the create button
   */
  function findCreateButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Create'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
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
   * Tests that the CreateProduct is invalid if product name consists of letters, numbers, spaces and punctuation.
   */
  it.each(validCharacters)('Valid if product has name "%s"', async (name) => {
    await populateAllFields();
    await wrapper.setData({
      product: name,
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });

  /**
   * Tests that the CreateProduct is invalid if product name has a character that is not a letter, number, space or punctuation.
   */
  it.each(invalidCharacters.concat(whitespaceCharacters))('Invalid if product has name "%s"', async (name) => {
    await populateAllFields();
    await wrapper.setData({
      product: name,
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
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
   * Tests that the CreateProduct is invalid if product description consists of letters, numbers, whitespace and punctuation.
   */
  it.each(validCharacters.concat(whitespaceCharacters))('Valid if product has description "%s"', async (description) => {
    await populateAllFields();
    await wrapper.setData({
      description,
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });

  /**
   * Tests that the CreateProduct is invalid if product description has a character that is not a letter, number, whitespace or punctuation.
   */
  it.each(invalidCharacters)('Invalid if product has description "%s"', async (description) => {
    await populateAllFields();
    await wrapper.setData({
      description,
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
   * Tests that the CreateProduct is invalid if product manufacturer consists of letters, numbers, spaces and punctuation.
   */
  it.each(validCharacters)('Valid if product has manufacturer "%s"', async (manufacturer) => {
    await populateAllFields();
    await wrapper.setData({
      manufacturer,
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });

  /**
   * Tests that the CreateProduct is invalid if product manufacturer has a character that is not a letter, number, space or punctuation.
   */
  it.each(invalidCharacters.concat(whitespaceCharacters))('Invalid if product has manufacturer "%s"', async (manufacturer) => {
    await populateAllFields();
    await wrapper.setData({
      manufacturer,
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
   * Tests that the CreateProduct is invalid if the product code is too long
   */
  it('Invalid if product code is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: 'a'.repeat(16),
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

  /**
   * Tests that when the close button is pressed the "closeDialog" event is emitted, this should
   * also result in the dialog getting closed.
   */
  it('When the close button is pressed then the "closeDialog" event should be emitted', async () => {
    await findCloseButton().trigger('click'); // Click close button

    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  /**
   * Tests that when the create button is pressed and the api call is successful that the parameters
   * are passed to the api function and the dialog is closed.
   */
  it('When the create button is pressed then an api call should be made and is successful', async () => {
    await populateAllFields();
    createProduct.mockResolvedValue(undefined); // Ensure that the operation is successful

    await Vue.nextTick();

    await findCreateButton().trigger('click'); // Click create button

    await Vue.nextTick();

    expect(createProduct).toBeCalledWith(90, {
      id: 'ABC-XYZ-012-789',
      name: 'Product Name',
      description: 'Product Description',
      manufacturer: 'Product Manufacturer',
      recommendedRetailPrice: 100,
    });
    expect(wrapper.emitted().closeDialog).toBeTruthy(); // The dialog should close
  });

  /**
   * Tests that if the create button is pressed, but the api returns an error. Then this error
   * should be shown
   */
  it('When the create button is pressed and the api returns an error then the error should be shown', async () => {
    await populateAllFields();
    createProduct.mockResolvedValue('test_error_message'); // Ensure that the operation fails

    await Vue.nextTick();

    await findCreateButton().trigger('click'); // Click create button

    await flushQueue();

    // The appWrapper is tested for the text, because the dialog content is not in the dialog
    // element.
    expect(appWrapper.text()).toContain('test_error_message');
    expect(wrapper.emitted().closeDialog).toBeFalsy(); // The dialog should stay open
  });

  /**
   * Tests that if the create button is pressed, but the api returns an 'Product code unavailable'
   * then the form should become invalid. Since the product code cannot be used.
   */
  it('When the create button is pressed and the api says that the product code is unavailable then the form should become invalid', async () => {
    await populateAllFields();
    createProduct.mockResolvedValue('Product code unavailable'); // Ensure that the operation fails

    await Vue.nextTick();

    await findCreateButton().trigger('click'); // Click create button

    await flushQueue();

    expect(wrapper.vm.valid).toBeFalsy();
    expect(wrapper.emitted().closeDialog).toBeFalsy(); // The dialog should stay open
  });

  /**
   * Tests that the values of the code and symbol attributes returned by the mocked currencyFromCountry
   * method are present in the Recommended Retail Price field.
   */
  it('RRP field contains currency code and symbol recieved from API', () => {
    const fields = wrapper.findAllComponents({ name: 'v-text-field' });
    const rrpFields = fields.filter(field => field.text().includes('Recommended Retail Price'));
    expect(rrpFields.length).toBe(1);
    const rrpField = rrpFields.at(0);
    expect(rrpField.text()).toContain('Currency symbol');
    expect(rrpField.text()).toContain('Currency code');
  }) 
});
