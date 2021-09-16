import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ProductForm from '@/components/BusinessProfile/ProductForm.vue';
import {castMock, flushQueue, findButtonWithText} from './utils';
import {createProduct as createProduct1, modifyProduct as modifyProduct1, Product} from "@/api/internal-product";
import { currencyFromCountry } from '@/api/currency';

jest.mock('@/api/internal', () => ({
  createProduct: jest.fn(),
  modifyProduct: jest.fn(),
  getBusiness: jest.fn(() => {
    return {
      address: {
        country: 'New Zealand',
      }
    };
  })
}));

jest.mock('@/api/currency', () => ({
  currencyFromCountry: jest.fn(() => {
    return {
      code: 'Currency code',
      name: 'Currency name',
      symbol: 'Currency symbol',
      errorMessage: "Some error message"
    };
  })
}));


const createProduct = castMock(createProduct1);
const modifyProduct = castMock(modifyProduct1);

Vue.use(Vuetify);

// Characters that are in the set of letters, numbers, spaces and punctuation.
const validCharacters = [" ", ":", ",", "7", "é", "树", "A"];
// Characters that are not a letter, number, space or punctuation.
const invalidCharacters = ["\uD83D\uDE02", "♔"];
// Characters that are whitespace not including the space character.
const whitespaceCharacters = ["\n", "\t"];

const localVue = createLocalVue();

describe('ProductForm.vue - Create', () => {
  // Container for the wrapper around ProductForm
  let appWrapper: Wrapper<any>;

  // Container for the ProductForm under test
  let wrapper: Wrapper<any>;

  /**
   * Sets up the test ProductForm instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around ProductForm with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { ProductForm },
      template: '<div data-app><ProductForm :previousProduct="previousProduct" :businessId="90"/></div>',
      data() {
        return {
          previousProduct: undefined,
        };
      }
    });

    // Put the ProductForm component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(ProductForm);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the ProductForm component is removed from the global document
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
   * Populates all fields of the ProductForm form
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
   * Finds the close button in the ProductForm form
   *
   * @returns A Wrapper around the close button
   */
  const findCloseButton = () => findButtonWithText(wrapper, 'Close');

  /**
   * Finds the create button in the ProductForm form
   *
   * @returns A Wrapper around the create button
   */
  const findCreateButton = () => findButtonWithText(wrapper, 'Create');

  it('Valid if all required fields are provided', async () => {
    await populateRequiredFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if all fields are provided', async () => {
    await populateAllFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it.each(validCharacters)('Valid if product has name "%s"', async (name) => {
    await populateAllFields();
    await wrapper.setData({
      product: name,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it.each(invalidCharacters.concat(whitespaceCharacters))(`Invalid if product has name "%s"`, async (name) => {
    await populateAllFields();
    await wrapper.setData({
      product: name,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if no product name', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      product: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product name is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      product: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it.each(validCharacters.concat(whitespaceCharacters))('Valid if product has description "%s"', async (description) => {
    await populateAllFields();
    await wrapper.setData({
      description,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it.each(invalidCharacters)('Invalid if product has description "%s"', async (description) => {
    await populateAllFields();
    await wrapper.setData({
      description,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if description is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'a'.repeat(201),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it.each(validCharacters)('Valid if product has manufacturer "%s"', async (manufacturer) => {
    await populateAllFields();
    await wrapper.setData({
      manufacturer,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it.each(invalidCharacters.concat(whitespaceCharacters))('Invalid if product has manufacturer "%s"', async (manufacturer) => {
    await populateAllFields();
    await wrapper.setData({
      manufacturer,
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if manufacturer is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      manufacturer: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product price is not a number', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: 'not a number',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product price is negative', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '-10',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product price is too high', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '100001',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product price has too many digits after the decimal', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '3.141',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product price has a single decimal digit', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      recommendedRetailPrice: '3.1',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if no product code is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product code is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: 'a'.repeat(16),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product code uses lowercase letters', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: 'aaaaaaaaa',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if product code has spaces', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      productCode: '          ',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('When the close button is pressed then the "closeDialog" event should be emitted', async () => {
    await findCloseButton().trigger('click'); // Click close button
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

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

  it('When the create button is pressed and the api says that the product code is unavailable then the form should become invalid', async () => {
    await populateAllFields();
    createProduct.mockResolvedValue('Product code unavailable'); // Ensure that the operation fails
    await Vue.nextTick();
    await findCreateButton().trigger('click'); // Click create button
    await flushQueue();
    expect(wrapper.vm.valid).toBeFalsy();
    expect(wrapper.emitted().closeDialog).toBeFalsy(); // The dialog should stay open
  });

  it('RRP field contains currency code and symbol recieved from API', () => {
    const fields = wrapper.findAllComponents({ name: 'v-text-field' });
    const rrpFields = fields.filter(field => field.text().includes('Recommended Retail Price'));
    expect(rrpFields.length).toBe(1);
    const rrpField = rrpFields.at(0);
    expect(rrpField.text()).toContain('Currency symbol');
    expect(rrpField.text()).toContain('Currency code');
  });

  it('If the error message is not undefined, there should be an hint message that appears in the dialog box', async () => {
    expect(wrapper.vm.currency.errorMessage).toBe("Some error message");
  });
});


describe('ProductForm.vue - Modify', () => {
  // Container for the wrapper around ProductForm
  let appWrapper: Wrapper<any>;

  // Container for the ProductForm under test
  let wrapper: Wrapper<any>;

  const previousProduct: Product = {
    id: 'TEST-ID',
    name: 'Test product name',
    description: 'Test product description',
    manufacturer: 'Test product manufacturer',
    recommendedRetailPrice: 199,
    images: [],
  };

  /**
   * Sets up the test ProductForm instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around ProductForm with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { ProductForm },
      template: '<div data-app><ProductForm :previousProduct="previousProduct" :businessId="90"/></div>',
      data() {
        return {
          previousProduct,
        };
      }
    });

    // Put the ProductForm component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(ProductForm);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the ProductForm component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Finds the save button in the ProductForm form
   *
   * @returns A Wrapper around the save button
   */
  const findSaveButton = () => findButtonWithText(wrapper, 'Save');

  it('Form has the correct title', () => {
    expect(appWrapper.text()).toContain(`Update ${previousProduct.id}`);
  });

  it('Product fields are populated', () => {
    expect(wrapper.vm.productCode).toBe(previousProduct.id);
    expect(wrapper.vm.product).toBe(previousProduct.name);
    expect(wrapper.vm.description).toBe(previousProduct.description);
    expect(wrapper.vm.manufacturer).toBe(previousProduct.manufacturer);
  });

  it('When the save button is pressed then an api call should be made and is successful', async () => {
    modifyProduct.mockResolvedValue(undefined); // Ensure that the operation is successful
    await wrapper.setData({
      productCode: 'FOO-BAR',
    });
    await Vue.nextTick();

    await findSaveButton().trigger('click'); // Press save

    await flushQueue();

    expect(modifyProduct).toBeCalledWith(90, previousProduct.id, {
      id: 'FOO-BAR',
      name: previousProduct.name,
      description: previousProduct.description,
      manufacturer: previousProduct.manufacturer,
      recommendedRetailPrice: previousProduct.recommendedRetailPrice,
    });
    expect(wrapper.emitted().closeDialog).toBeTruthy(); // The dialog should close
  });

  it('When the save button is pressed and the api returns an error then the error should be shown', async () => {
    modifyProduct.mockResolvedValue('test_error_message'); // Ensure that the operation fails
    await findSaveButton().trigger('click'); // Click save button
    await flushQueue();
    // The appWrapper is tested for the text, because the dialog content is not in the dialog
    // element.
    expect(appWrapper.text()).toContain('test_error_message');
    expect(wrapper.emitted().closeDialog).toBeFalsy(); // The dialog should stay open
  });

  it('When the save button is pressed and the api says that the product code is unavailable then the form should become invalid', async () => {
    modifyProduct.mockResolvedValue('Product code unavailable'); // Ensure that the operation fails
    await findSaveButton().trigger('click'); // Click save button
    await flushQueue();
    expect(wrapper.vm.valid).toBeFalsy();
    expect(wrapper.emitted().closeDialog).toBeFalsy(); // The dialog should stay open
  });
});

