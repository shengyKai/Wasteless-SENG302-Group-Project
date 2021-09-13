import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import Marketplace from '@/components/marketplace/Marketplace.vue';
import {castMock, flushQueue} from './utils';
import {getStore, resetStoreForTesting} from '@/store';
import {User} from "@/api/internal-user";
import {getMarketplaceCardsBySection as getMarketplaceCardsBySection1, MarketplaceCard} from "@/api/internal-marketplace";
import {searchKeywords} from "@/api/internal-keyword";

jest.mock('@/api/internal', () => ({
  getMarketplaceCardsBySection: jest.fn(),
  searchKeywords: jest.fn(),
}));

const getMarketplaceCardsBySection = castMock(getMarketplaceCardsBySection1);
const getKeywords = castMock(searchKeywords);
Vue.use(Vuetify);

const localVue = createLocalVue();
localVue.use(Vuex);

const testUser: User = {
  id: 2,
  firstName: 'test_firstname',
  lastName: 'test_lastname',
  email: 'test_email',
  homeAddress: { country: 'test_country', city: 'test_city', district: 'test_district'},
};

const testMarketplaceCard: MarketplaceCard = {
  id: 1,
  creator: testUser,
  section: 'ForSale',
  created: '2021-03-10',
  lastRenewed: '2021-03-10',
  title: 'test_card_title',
  description: 'test_card_description',
  keywords: [{id: 3, created: "2021-02-10", name: 'test_keyword_1'}, {id: 4, created: "2021-02-10", name: 'test_keyword_2'}],
};

describe('Marketplace.vue', () => {
  // Container for the Marketplace under test
  let wrapper: Wrapper<any>;

  /**
   * Creates the wrapper for the Marketplace component.
   * This must be called before using the Marketplace wrapper.
   */
  function createWrapper() {
    resetStoreForTesting();
    let store = getStore();
    store.state.user = testUser;
    wrapper = mount(Marketplace, {
      localVue,
      vuetify: new Vuetify(),
      store: store,
    });
  }

  beforeEach(() => {
    getMarketplaceCardsBySection.mockResolvedValue({
      count: 1,
      results: [testMarketplaceCard],
    });
    getKeywords.mockResolvedValue([{
      id: 1,
      name: "dance",
      created: "2020-01-02",
    }]);
    createWrapper();
  });

  it('When card order field is changed, API call to get cards is made with new order field', async () => {
    await flushQueue();
    const prevCalls = getMarketplaceCardsBySection.mock.calls.length;
    expect(wrapper.vm.orderBy).toBe('lastRenewed');

    wrapper.setData({
      orderBy: "title",
    });
    await flushQueue();

    expect(getMarketplaceCardsBySection.mock.calls.length).toBe(prevCalls + 3);
    expect(getMarketplaceCardsBySection.mock.calls[prevCalls][3]).toBe('title');
    expect(getMarketplaceCardsBySection.mock.calls[prevCalls+1][3]).toBe('title');
    expect(getMarketplaceCardsBySection.mock.calls[prevCalls+2][3]).toBe('title');
  });

  it('When card order is reversed, API call to get cards is made with reversed order', async () => {
    await flushQueue();
    const prevCalls = getMarketplaceCardsBySection.mock.calls.length;
    expect(wrapper.vm.reverse).toBe(true);

    wrapper.setData({
      reverse: false,
    });
    await flushQueue();

    expect(getMarketplaceCardsBySection.mock.calls.length).toBe(prevCalls + 3);
    expect(getMarketplaceCardsBySection.mock.calls[prevCalls][4]).toBe(false);
    expect(getMarketplaceCardsBySection.mock.calls[prevCalls+1][4]).toBe(false);
    expect(getMarketplaceCardsBySection.mock.calls[prevCalls+2][4]).toBe(false);
  });


});