import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import ExpiryEvent from '@/components/home/newsfeed/ExpiryEvent.vue';
import MarketplaceCard from "@/components/cards/MarketplaceCard.vue";
import * as events from '@/api/events';

import Vuex, {Store} from 'vuex';
import {getStore, resetStoreForTesting, StoreData} from '@/store';
import {castMock, makeTestUser, findButtonWithText} from '../utils';
import {extendMarketplaceCardExpiry as extendMarketplaceCardExpiry1} from "@/api/marketplace";

Vue.use(Vuetify);

jest.mock('@/api/marketplace', () => ({
  extendMarketplaceCardExpiry: jest.fn(),
}));

jest.mock('@/api/events', () => ({
  getEvents: jest.fn(),
  updateEventAsRead: jest.fn(),
}));

jest.mock('@/components/utils/Methods/synchronizedTime', () => ({
  now : new Date("2021-01-02T11:00:00Z")
}));

const extendMarketplaceCardExpiry = castMock(extendMarketplaceCardExpiry1);
const getEvents = castMock(events.getEvents);

describe('ExpiryEvent.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  // The global store to be used
  let store: Store<StoreData>;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = makeTestUser(1);

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append (app);

    wrapper = mount(ExpiryEvent, {
      localVue,
      vuetify,
      store,
      components: {
        MarketplaceCard
      },
      propsData: {
        event: {
          created: "2021-01-01T12:00:00Z",
          card: {
            id: 101,
            title: "Card title",
            displayPeriodEnd: "2021-01-02T12:00:00Z",
            description: "Card description",
            creator: {
              id: 77
            }
          }
        },
      }
    });

    getEvents.mockResolvedValue([]);
  });

  afterEach(() => {
    wrapper.destroy();
  });

  /**
     * Finds the delay expiry button in the ExpiryEvent.
     *
     * @returns A Wrapper around the delay expiry.
     */
  const findDelayButton = () => findButtonWithText(wrapper, 'Delay expiry');

  /**
     * Finds the button which controls whether the component showing the card is expanded or hidden.
     *
     * @returns A Wrapper around the expand button.
     */
  const findExpandButton = () => findButtonWithText(wrapper, 'card');

  /**
     * Finds the marketplace card displayed on the event.
     *
     * @returns A Wrapper around the marketplace card.
     */
  function findMarketplaceCard() {
    const marketplaceCard = wrapper.findAllComponents({ name: 'MarketplaceCard' });
    expect(marketplaceCard.length).toBe(1);
    return marketplaceCard.at(0);
  }

  describe('Expiry has not been delayed', () => {
    beforeEach(async () => {
      await wrapper.setData({
        delayed: false,
      });
    });

    it("Title contains expiry message", () => {
      expect(wrapper.vm.title).toBe('Your marketplace card is about to expire');
    });

    it("Text contains expiry message ", () => {
      expect(wrapper.vm.text).toBe('Your card \'Card title\' will expire in 1h 0m 0s. Do you want to delay the expiry by two weeks?');
    });

    it("Delay expiry button is visible", () => {
      expect(findExpandButton().isVisible()).toBeTruthy;
    });

    it("Clicking the delay expiry button triggers the api request to delay the expiry", async () => {
      await findDelayButton().trigger('click');
      await Vue.nextTick();
      expect(extendMarketplaceCardExpiry).toBeCalledWith(101);
    });

    describe('A request to delay expiry is made', () => {

      it("When a successful response is received, the event shows that the expiry has been delayed", async () => {
        extendMarketplaceCardExpiry.mockResolvedValueOnce(undefined);
        await findDelayButton().trigger('click');
        await Vue.nextTick();
        expect(wrapper.vm.delayed).toBeTruthy();
        expect(wrapper.vm.errorMessage).toBe(undefined);
      });

      it("When an error response is received, the event shows an error", async () => {
        extendMarketplaceCardExpiry.mockResolvedValueOnce("Error message");
        await findDelayButton().trigger('click');
        await Vue.nextTick();
        expect(wrapper.vm.delayed).toBeFalsy();
        expect(wrapper.vm.errorMessage).toBe("Error message");
      });
    });

  });

  describe('Expiry has been delayed', () => {
    beforeEach(async () => {
      await wrapper.setData({
        delayed: true,
      });
    });

    it("Title contains expiry delayed message", () => {
      expect(wrapper.vm.title).toBe('You have delayed your card\'s expiry date');
    });

    it("Text contains expiry delayed message ", () => {
      expect(wrapper.vm.text).toBe('The expriy date of your card \'Card title\' was delayed by two weeks.');
    });

    it("Delay expiry button is not visible", () => {
      expect(findExpandButton().isVisible()).toBeFalsy;
    });

  });

  describe('Expanded to display marketplace card', () => {
    beforeEach(async () => {
      await wrapper.setData({
        viewCard: true,
      });
    });

    it("Marketplace card is visible", () => {
      expect(findMarketplaceCard().isVisible()).toBeTruthy();
    });

    it("Expand button text is 'Hide card'", () => {
      expect(findExpandButton().text()).toBe('Hide card');
    });

    it("When expand button is clicked, card is collapsed", async () => {
      const button = findExpandButton();
      await button.trigger('click');
      expect(findMarketplaceCard().isVisible()).toBeFalsy();
    });

  });

  describe('Collapsed to hide marketplace card', () => {
    beforeEach(async () => {
      await wrapper.setData({
        viewCard: false,
      });
    });

    it("Marketplace card is not visible", async () => {
      expect(findMarketplaceCard().isVisible()).toBeFalsy();
    });

    it("Expand button text is 'View card'", () => {
      expect(findExpandButton().text()).toBe('View card');
    });

    it("When expand button is clicked, card is expanded", async () => {
      const button = findExpandButton();
      await button.trigger('click');
      expect(findMarketplaceCard().isVisible()).toBeTruthy();
    });

  });
});
