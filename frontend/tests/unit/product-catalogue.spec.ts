import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import ProductCatalogue from '@/components/ProductCatalogue.vue';
import ProductCatalogueItem from '@/components/cards/ProductCatalogueItem.vue';
import { Product } from '@/api/internal';
import * as api from '@/api/internal';
import { castMock, flushQueue } from './utils';

jest.mock('@/api/internal', () => ({
  getProducts: jest.fn(),
  searchCatalogue: jest.fn()
}));

// Debounce adds a delay on updates to search query that we need to get rid of
jest.mock('@/utils', () => ({
  debounce: (func: (() => void)) => func,
}));

const getProducts = castMock(api.getProducts);
const searchCatalogue = castMock(api.searchCatalogue);

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
  let result: api.SearchResults<Product> = {
    results : [],
    count   : count,
  };

  for (let i = 0; i<count; i++) {
    result.results.push({
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
  function createGetProductWrapper() {
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
   * Creates the wrapper for the ProductCatalogue component.
   * This must be called before using the ProductCatalogue wrapper.
   */
  function createSearchCatalogueWrapper() {
    wrapper = mount(ProductCatalogue, {
      stubs: ['router-link', 'router-view', 'ProductCatalogueItem'],
      mocks: {
        $route: {
          params: {
            id: '100',
          }
        },
      },
      data() {
        return {
          searchQuery: "something",
          searchBy: ["productCode", "name"]
        };
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
  function setResults(products: api.SearchResults<Product>) {
    getProducts.mockResolvedValue(products);
    searchCatalogue.mockResolvedValue(products);
  }

  /**
   * Finds the error message component if it exists
   *
   * @returns Wrapper for the error component if it exists
   */
  function findErrorBox() {
    return wrapper.findComponent({name: 'v-alert'});
  }

  afterEach(() => {
    jest.clearAllMocks();
  });

  /**
   * Tests that when initially opened that the products are queried
   */
  it('The products from the business id are queried', () => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    expect(getProducts).toBeCalledWith(100, 1, RESULTS_PER_PAGE, 'productCode', false);
  });

  it('The search results should be displayed somewhere', async () => {
    let products = createTestProducts(5);
    setResults(products);
    createGetProductWrapper();
    // Flush queue is used instead of Vue.nextTick() since this will wait for everything to finish
    // instead of a single event.
    await flushQueue();
    const shownProducts = wrapper
      .findAllComponents(ProductCatalogueItem)
      .wrappers
      .map(searchResult => searchResult.props().product);
    expect(shownProducts).toStrictEqual(products.results);
  });

  it('If there is an error then the error should be displayed', async () => {
    getProducts.mockResolvedValue('test_error');
    createGetProductWrapper();
    await flushQueue();
    expect(findErrorBox().text()).toEqual('test_error');
  });

  it('If the error is dismissed then the error should disappear', async () => {
    getProducts.mockResolvedValue('test_error');
    createGetProductWrapper();
    await flushQueue();
    // Finds dismiss button and clicks it
    findErrorBox().findComponent({name: 'v-btn' }).trigger('click');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeFalsy();
  });

  it('If there are many pages then there should be a pagination component with many pages', async () => {
    let testResult = createTestProducts(RESULTS_PER_PAGE);
    testResult.count = 100;
    setResults(testResult);

    createGetProductWrapper();
    await flushQueue();
    let pagination = wrapper.findComponent({ name: 'v-pagination' });
    expect(pagination.props().length).toBe(Math.ceil(100 / RESULTS_PER_PAGE));
    expect(pagination.props().disabled).toBe(false);
    expect(pagination.props().value).toBe(1);
  });

  it('If there are results then there should be a message informing the buisness admin how many', async () => {
    let testResult = createTestProducts(RESULTS_PER_PAGE);
    testResult.count = 100;
    setResults(testResult);

    createGetProductWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain(`Displaying 1 - ${RESULTS_PER_PAGE} of 100 results`);
  });

  it('If there are no results then there should be a message informing the buisness admin of that', async () => {
    setResults(createTestProducts(0));
    createGetProductWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain('There are no results to show');
  });

  it('If there is a change in the orderBy attribute, the product results will be updated', async () => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    await wrapper.setData({
      orderBy: "description"
    })
    await Vue.nextTick();
    expect(getProducts).toHaveBeenCalledWith(100, 1, RESULTS_PER_PAGE, 'description', false);
  });

  it('If there is a change in the reverse attribute, the product results will be updated', async () => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    await wrapper.setData({
      reverse: true
    })
    await Vue.nextTick();
    expect(getProducts).toHaveBeenCalledWith(100, 1, RESULTS_PER_PAGE, 'productCode', true);
  });

  it('If there is a change in the currentPage attribute, the product results will be updated', async () => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    await wrapper.setData({
      currentPage: 2
    })
    await Vue.nextTick();
    expect(getProducts).toHaveBeenCalledWith(100, 2, RESULTS_PER_PAGE, 'productCode', false);
  });

  it('If there is a change in the resultsPerPage attribute, the product results will be updated', async () => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    await wrapper.setData({
      resultsPerPage: 5
    })
    await Vue.nextTick();
    expect(getProducts).toHaveBeenCalledWith(100, 1, 5, 'productCode', false);
  });

  it("If the search query is empty, getProducts will be called", async() => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    expect(getProducts).toHaveBeenCalled();
    expect(searchCatalogue).not.toHaveBeenCalled();
  });

  it("If the search query is not empty, searchCatalogue will be called", async() => {
    setResults(createTestProducts(5));
    createSearchCatalogueWrapper();
    expect(getProducts).not.toHaveBeenCalled();
    expect(searchCatalogue).toHaveBeenCalled();
  });

  it("If going through a normal workflow, both getProduct and searchCatalogue will be called", async() => {
    setResults(createTestProducts(5));
    createGetProductWrapper();
    await wrapper.setData({
      searchQuery: "something"
    });
    await Vue.nextTick();
    // getProducts will be called first upon the page's initialisation
    expect(getProducts).toHaveBeenCalled();
    // searchCatalogue will then be called afterwards when it realises that the searchQuery attribute
    // is not empty
    expect(searchCatalogue).toHaveBeenCalled();
  });

  it("If the user uses the searchQuery and searchBy, searchCatalogue will be called with the respective parameters", async() => {
    setResults(createTestProducts(5));
    createSearchCatalogueWrapper();
    expect(searchCatalogue).toHaveBeenCalledWith(100, "something", 1, RESULTS_PER_PAGE, ["productCode", "name"], "productCode", false);
  });
});
