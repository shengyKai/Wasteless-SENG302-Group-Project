import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import SearchResults from '@/components/SearchResults.vue';
import SearchResultItem from '@/components/SearchResultItem.vue';
import { search, getSearchCount, User } from '@/api';



jest.mock('@/api', () => ({
  search: jest.fn(),
  getSearchCount: jest.fn(),
}));

// Debounce adds a delay on updates to search query that we need to get rid of
jest.mock('@/utils', () => ({
  debounce: (func: (() => void)) => func,
}));

/**
 * Returns a promise to a point where the current event queue is empty.
 *
 * @returns Empty Promise
 */
function flushQueue() {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

/**
 * Reinterprets the input argument as a jest mock.
 *
 * @param func Function that is mocked
 * @returns Input argument interpreted as a jest mock
 */
function castMock<T, Y extends any[]>(func: (...args: Y) => T) {
  return <jest.Mock<T, Y>><unknown>func;
}

const searchMock = castMock(search);
const getSearchCountMock = castMock(getSearchCount);

Vue.use(Vuetify);

const localVue = createLocalVue();

localVue.use(VueRouter);
localVue.use(Vuex);
const router = new VueRouter();

const RESULTS_PER_PAGE = 10;

// Sets the initial search query
router.push({ path: 'unimportant', query: { query: 'test_query' }});

/**
 * Creates a list of unique test users
 *
 * @param count Number of users to create
 * @returns List of test users
 */
function createTestUsers(count: number) {
  let result: User[] = [];

  for (let i = 0; i<count; i++) {
    result.push({
      id: i,
      firstName: 'test_firstname' + i,
      lastName: 'test_lastname' + i,
      email: 'test_email' + i,
      dateOfBirth: '1/1/1900',
      homeAddress: { country: 'test_country' + i },
    });
  }
  return result;
}

describe('SearchResults.vue', () => {
  // Container for the SearchResults under test
  let wrapper: Wrapper<any>;


  /**
   * Creates the wrapper for the SearchResults component.
   * This must be called before using the SearchResults wrapper.
   */
  function createWrapper() {
    wrapper = mount(SearchResults, {
      localVue,
      router,
      vuetify: new Vuetify(),
    });
  }

  /**
   * Sets the mock api results.
   *
   * @param users Users on the current page to use for the mock results
   * @param testCount The mock number of total users for this search
   */
  function setResults(users: User[], totalCount?: number) {
    searchMock.mockResolvedValue(users);
    getSearchCountMock.mockResolvedValue(totalCount !== undefined ? totalCount : users.length);
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
   * Tests that when initially opened that the initial search query from the route is searched
   */
  it('The search query passed in from the url is searched', () => {
    setResults(createTestUsers(5));
    createWrapper();
    expect(searchMock).toBeCalledWith('test_query', 1, RESULTS_PER_PAGE, 'relevance', false);
  });

  /**
   * Tests that the search results are shown
   */
  it('The search results should be displayed somewhere', async () => {
    let users = createTestUsers(5);
    setResults(users);

    createWrapper();

    // Flush queue is used instead of Vue.nextTick() since this will wait for everything to finish
    // instead of a single event.
    await flushQueue();

    const shownUsers = wrapper
      .findAllComponents(SearchResultItem)
      .wrappers
      .map(searchResult => searchResult.props().user);
    expect(shownUsers).toStrictEqual(users);
  });

  /**
   * Tests that errors are shown
   */
  it('If there is an error then the error should be displayed', async () => {
    searchMock.mockResolvedValue('test_error');

    createWrapper();

    await flushQueue();

    expect(findErrorBox().text()).toEqual('test_error');
  });

  /**
   * Tests that errors are dismissable
   */
  it('If the error is dismissed then the error should disappear', async () => {
    searchMock.mockResolvedValue('test_error');

    createWrapper();

    await flushQueue();

    // Finds dismiss button and clicks it
    findErrorBox().findComponent({name: 'v-btn' }).trigger('click');

    await Vue.nextTick();

    expect(findErrorBox().exists()).toBeFalsy();
  });

  /**
   * Tests the status text is 'There are no results to show' when there are no results
   */
  it('If there are no results then there should be a message informing the user of that', async () => {
    setResults([]);

    createWrapper();

    await flushQueue();

    expect(wrapper.text()).toContain('There are no results to show');
  });

  /**
   * Tests that the status text contains the number of results
   */
  it('If there are results then there should be a message informing the user how many', async () => {
    setResults(createTestUsers(RESULTS_PER_PAGE), 100);

    createWrapper();

    await flushQueue();

    expect(wrapper.text()).toContain(`Displaying 1 - ${RESULTS_PER_PAGE} of 100 results`);
  });

  /**
   * Tests that the search query is updated when the search input is updated
   */
  it('The search query should update as the search box is modified', async () => {
    setResults(createTestUsers(5));
    createWrapper();

    await flushQueue();

    // Update the search box
    const searchBox = wrapper.findComponent({ name: 'v-text-field' });
    await searchBox.findAll('input').at(0).setValue('new_test_query');

    expect(searchMock).lastCalledWith('new_test_query', 1, RESULTS_PER_PAGE, 'relevance', false);
  });

  /**
   * Tests that the pagination component exists and has the right number of pages
   */
  it('If there are many pages then there should be a pagination component with many pages', async () => {
    setResults(createTestUsers(RESULTS_PER_PAGE), 100);
    createWrapper();

    await flushQueue();

    let pagination = wrapper.findComponent({ name: 'v-pagination' });
    expect(pagination.props().length).toBe(Math.ceil(100 / RESULTS_PER_PAGE));
    expect(pagination.props().disabled).toBe(false);
    expect(pagination.props().value).toBe(1);
  });
});
