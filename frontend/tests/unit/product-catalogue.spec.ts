import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import ProductCatalogue from '@/components/ProductCatalogue.vue';
import ProductCatalogueItem from '@/components/ProductCatalogueItem.vue';
import { Product } from '@/api/internal';
import * as api from '@/api/internal';
import { castMock, flushQueue } from './utils';



jest.mock('@/api/internal', () => ({
  getProducts: jest.fn(),
}));

const getProducts = castMock(api.getProducts);

Vue.use(Vuetify);

const localVue = createLocalVue();
localVue.use(Vuex);

const RESULTS_PER_PAGE = 10;

/**
 * Creates a list of unique test products
 *
 * @param count Number of products to create
 * @returns List of test products
 */
function createTestProducts(count: number) {
  let result: Product[] = [];

  for (let i = 0; i<count; i++) {
    result.push({
      id: 'product_code' + i,
      name: 'product_name' + i,
      description: 'product_description' + i,
      manufacturer: 'product_manufacturer' + i,
      recommendedRetailPrice: i,
      images: [],
    });
  }
  return result;
}

describe('ProductCatalogue.vue', () => {
  // Container for the ProductCatalogue under test
  let wrapper: Wrapper<any>;


  /**
   * Creates the wrapper for the ProductCatalogue component.
   * This must be called before using the ProductCatalogue wrapper.
   */
  function createWrapper() {
    wrapper = mount(ProductCatalogue, {
      stubs: ['router-link', 'router-view', 'ProductCatalogueItem'],
      mocks: {
        $route: {
          params: {
            id: '100',
          }
        },
      },
      localVue,
      vuetify: new Vuetify(),
    });
  }

  /**
   * Sets the mock api results.
   *
   * @param products Products on the current page to use for the mock results
   */
  function setResults(products: Product[]) {
    getProducts.mockResolvedValue(products);
  }

  /**
   * Finds the error message component if it exists
   *
   * @returns Wrapper for the error component if it exists
   */
  function findErrorBox() {
    return wrapper.findComponent({name: 'v-alert'});
  }

  /**
   * Tests that when initially opened that the products are queried
   */
  it('The products from the business id are queried', () => {
    setResults(createTestProducts(5));
    createWrapper();
    expect(getProducts).toBeCalledWith(100, 1, RESULTS_PER_PAGE, 'productCode', false);
  });

  /**
   * Tests that the product results are shown
   */
  it('The search results should be displayed somewhere', async () => {
    let products = createTestProducts(5);
    setResults(products);

    createWrapper();

    // Flush queue is used instead of Vue.nextTick() since this will wait for everything to finish
    // instead of a single event.
    await flushQueue();

    const shownProducts = wrapper
      .findAllComponents(ProductCatalogueItem)
      .wrappers
      .map(searchResult => searchResult.props().product);
    expect(shownProducts).toStrictEqual(products);
  });

  /**
   * Tests that errors are shown
   */
  it('If there is an error then the error should be displayed', async () => {
    getProducts.mockResolvedValue('test_error');

    createWrapper();

    await flushQueue();

    expect(findErrorBox().text()).toEqual('test_error');
  });

  /**
   * Tests that errors are dismissable
   */
  it('If the error is dismissed then the error should disappear', async () => {
    getProducts.mockResolvedValue('test_error');

    createWrapper();

    await flushQueue();

    // Finds dismiss button and clicks it
    findErrorBox().findComponent({name: 'v-btn' }).trigger('click');

    await Vue.nextTick();

    expect(findErrorBox().exists()).toBeFalsy();
  });
});
