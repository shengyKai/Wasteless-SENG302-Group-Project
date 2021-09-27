import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import SearchBusinessPage from '@/components/SearchBusinessPage.vue';
import SearchBusinessResult from '@/components/cards/SearchBusinessResult.vue';
import {castMock, flushQueue} from './utils';
import {searchBusinesses as searchBusinesses1, Business} from "@/api/business";

jest.mock('@/api/business', () => ({
  searchBusinesses: jest.fn(),
  BUSINESS_TYPES: ['Accommodation and Food Services', 'Retail Trade', 'Charitable organisation', 'Non-profit organisation'],
}));

// Debounce adds a delay on updates to search query that we need to get rid of
jest.mock('@/utils', () => ({
  debounce: (func: (() => void)) => func,
}));

const searchBusinesses = castMock(searchBusinesses1);

Vue.use(Vuetify);

const localVue = createLocalVue();
localVue.use(Vuex);

const RESULTS_PER_PAGE = 10;

/**
 * Creates a list of unique test users
 *
 * @param count Number of users to create
 * @returns List of test users
 */
function createTestBusinesses(count: number) {
  let result: Business[] = [];

  for (let i = 0; i<count; i++) {
    result.push({
      id: i,
      name: 'test_name' + i,
      primaryAdministratorId: 3,
      businessType: "Accommodation and Food Services",
      address: { city: 'test_city' + i, country: 'test_country' + i },
      images: [],
      points: 5,
      rank: {
        name: 'bronze',
      }
    });
  }
  return result;
}

describe('SearchBusinessPage.vue', () => {
  // Container for the SearchResults under test
  let wrapper: Wrapper<any>;

  /**
   * Creates the wrapper for the SearchResults component.
   * This must be called before using the SearchResults wrapper.
   */
  function createWrapper() {
    wrapper = mount(SearchBusinessPage, {
      stubs: ['router-link', 'router-view'],
      mocks: {
        $route: {
          query: {
            query: 'test_query',
          },
        },
      },
      localVue,
      vuetify: new Vuetify(),
    });
  }

  /**
   * Sets the mock api results.
   *
   * @param users Businesses on the current page to use for the mock results
   * @param totalCount The mock number of total users for this search
   */
  function setResults(users: Business[], totalCount?: number) {
    searchBusinesses.mockResolvedValue({
      results: users,
      count: totalCount !== undefined ? totalCount : users.length
    });
  }

  /**
   * Finds the error message component if it exists
   *
   * @returns Wrapper for the error component if it exists
   */
  function findErrorBox() {
    return wrapper.findComponent({name: 'v-alert'});
  }

  it('The search results should be displayed somewhere', async () => {
    let users = createTestBusinesses(5);
    setResults(users);
    createWrapper();
    await wrapper.setData({searchQuery: "apple"});
    // Flush queue is used instead of Vue.nextTick() since this will wait for everything to finish
    // instead of a single event.
    await flushQueue();
    const shownBusinesses = wrapper
      .findAllComponents(SearchBusinessResult)
      .wrappers
      .map(searchResult => searchResult.props().business);
    expect(shownBusinesses).toStrictEqual(users);
  });

  it('If there is an error then the error should be displayed', async () => {
    searchBusinesses.mockResolvedValue('test_error');
    createWrapper();
    await wrapper.setData({searchQuery: "apple"});
    await flushQueue();
    expect(findErrorBox().text()).toEqual('test_error');
  });

  it('If the error is dismissed then the error should disappear', async () => {
    searchBusinesses.mockResolvedValue('test_error');
    createWrapper();
    await wrapper.setData({searchQuery: "apple"});
    await flushQueue();
    // Finds dismiss button and clicks it
    findErrorBox().findComponent({name: 'v-btn' }).trigger('click');
    await Vue.nextTick();
    expect(findErrorBox().exists()).toBeFalsy();
  });

  it('If there are no results then there should be a message informing the user of that', async () => {
    setResults([]);
    createWrapper();
    await flushQueue();
    expect(wrapper.text()).toContain('There are no results to show');
  });

  it('If there are results then there should be a message informing the user how many', async () => {
    setResults(createTestBusinesses(RESULTS_PER_PAGE), 100);
    createWrapper();
    await wrapper.setData({searchQuery: "apple"});
    await flushQueue();
    expect(wrapper.text()).toContain(`Displaying 1 - ${RESULTS_PER_PAGE} of 100 results`);
  });

  it('The search query should update as the search box is modified', async () => {
    setResults(createTestBusinesses(5));
    createWrapper();
    await flushQueue();
    // Update the search box
    await wrapper.setData({
      searchQuery: 'new_test_query',
    });
    await wrapper.vm.updateResults();
    expect(searchBusinesses).lastCalledWith('new_test_query', undefined, 1, RESULTS_PER_PAGE, 'name', false);
  });

  it('If there are many pages then there should be a pagination component with many pages', async () => {
    setResults(createTestBusinesses(RESULTS_PER_PAGE), 100);
    createWrapper();
    await wrapper.setData({searchQuery: "apple"});
    await flushQueue();
    let pagination = wrapper.findComponent({ name: 'v-pagination' });
    expect(pagination.props().length).toBe(Math.ceil(100 / RESULTS_PER_PAGE));
    expect(pagination.props().disabled).toBe(false);
    expect(pagination.props().value).toBe(1);
  });
});
