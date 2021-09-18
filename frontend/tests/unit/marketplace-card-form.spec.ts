import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import { ThisTypedComponentOptionsWithArrayProps } from 'vue/types/options';

import MarketplaceCardForm from '@/components/marketplace/MarketplaceCardForm.vue';
import {castMock, makeTestUser, findButtonWithText} from "./utils";
import {createMarketplaceCard as createMarketplaceCard1, MarketplaceCard} from "@/api/internal-marketplace";
import {searchKeywords as searchKeywords1} from "@/api/internal-keyword";
import { getStore, resetStoreForTesting } from '@/store';

jest.mock('@/api/internal-keyword', () => ({
  searchKeywords: jest.fn(),
}));
jest.mock('@/api/internal-marketplace', () => ({
  createMarketplaceCard: jest.fn(),
}));

const searchKeywords = castMock(searchKeywords1);
const createMarketplaceCard = castMock(createMarketplaceCard1);
Vue.use(Vuetify);
const localVue = createLocalVue();

//Characters that are in the set of letters, numbers, spaces and punctuation
const validCharacters = [
  "A",
  "7",
  " ",
  "树",
  ":",
  ",",
  "é",
];

// Characters that are not a letter, number, space or punctuation.
const invalidCharacters = [
  "\uD83D\uDE02",
  "♔",
];

/**
   * Sets up the test MarketplaceCardForm instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
describe('MarketplaceCardFrom.vue', () => {

  // Container for the wrapper around MarketplaceCardForm
  let appWrapper: Wrapper<any>;

  // Container for the MarketplaceCardForm under test
  let wrapper: Wrapper<any>;

  describe('Form is being used to create a marketplace card', () => {

    beforeEach(() => {
      const vuetify = new Vuetify();
      localVue.use(Vuex);
      // Creating wrapper around MarketplaceCardForm with data-app to appease vuetify
      const App = localVue.component('App', {
        components: { MarketplaceCardForm },
        template: '<div data-app><MarketplaceCardForm :user="user" :previousCard="previousCard"/></div>',
      });

      // Put the MarketplaceCardForm component inside a div in the global document,
      // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
      const elem = document.createElement('div');
      document.body.appendChild(elem);

      searchKeywords.mockResolvedValue([]);

      // We have to mock the $router.go method to prevent errors.
      appWrapper = mount(App, {
        stubs: ['router-link', 'router-view'],
        mocks: {
          $router: {
            go: () => {return;},
          }
        },
        localVue,
        vuetify,
        attachTo: elem,
        data() {
          return {
            user: makeTestUser(1),
            previousCard: undefined,
          };
        }
      });

      wrapper = appWrapper.getComponent(MarketplaceCardForm);
      expect(wrapper.vm.isCreate).toBeTruthy();
    });

    /**
   * Executes after every test case.
   *
   * This function makes sure that the MarketplaceCardForm component is removed from the global document
   */
    afterEach(() => {
      appWrapper.destroy();
    });

    /**
   * Finds the create button in the MarketplaceCardForm form
   *
   * @returns A Wrapper around the create button
   */
    const findButton = (text: string) => findButtonWithText(wrapper, text);

    it('Valid if all required fields are provided', async () => {
      await wrapper.setData({
        title: "Title",
        selectedSection: "ForSale",
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeTruthy();
      expect(findButton('Create Card').props().disabled).toBeFalsy();
    });

    it('Valid if all fields are provided', async () => {
      await wrapper.setData({
        title: "Title",
        selectedSection: "ForSale",
        description: "Description"
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeTruthy();
      expect(findButton('Create Card').props().disabled).toBeFalsy();
    });

    it('Invalid if title not provided', async () => {
      await wrapper.setData({
        title: "",
        selectedSection: "ForSale",
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeFalsy();
      expect(findButton('Create Card').props().disabled).toBeTruthy();
    });

    it.each(validCharacters)('Valid if title contains valid characters %s', async (character) => {
      await wrapper.setData({
        title: character,
        selectedSection: "ForSale",
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeTruthy();
      expect(findButton('Create Card').props().disabled).toBeFalsy();
    });

    it.each(invalidCharacters)('Invalid if title contains invalid characters %s', async (character) => {
      await wrapper.setData({
        title: character,
        selectedSection: "ForSale",
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeFalsy();
      expect(findButton('Create Card').props().disabled).toBeTruthy();
    });

    it('Invalid if title has over 50 characters', async () => {
      await wrapper.setData({
        title: "a".repeat(51),
        selectedSection: "ForSale",
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeFalsy();
      expect(findButton('Create Card').props().disabled).toBeTruthy();
    });

    it.each(validCharacters)('Valid if description contains valid characters %s', async (character) => {
      await wrapper.setData({
        title: "Title",
        selectedSection: "ForSale",
        description: character
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeTruthy();
      expect(findButton('Create Card').props().disabled).toBeFalsy();
    });

    it.each(invalidCharacters)('Invalid if description contains invalid characters %s', async (character) => {
      await wrapper.setData({
        title: "Title",
        selectedSection: "ForSale",
        description: character
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeFalsy();
      expect(findButton('Create Card').props().disabled).toBeTruthy();
    });

    it('Invalid if description has over 200 characters', async () => {
      await wrapper.setData({
        title: "Title",
        selectedSection: "ForSale",
        description: "a".repeat(201),
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeFalsy();
      expect(findButton('Create Card').props().disabled).toBeTruthy();
    });

    it('Invalid if section not provided', async () => {
      await wrapper.setData({
        title: "Title",
        selectedSection: undefined,
      });

      await Vue.nextTick();

      expect(wrapper.vm.valid).toBeFalsy();
      expect(findButton('Create Card').props().disabled).toBeTruthy();
    });

    it('Fields are initially blank', () => {
      expect(wrapper.vm.title).toBe("");
      expect(wrapper.vm.description).toBe("");
      expect(wrapper.vm.selectedSection).toBe(undefined);
      expect(wrapper.vm.selectedKeywords).toStrictEqual([]);
    });

    it('Modal title is "Create Marketplace Card"', () => {
      expect(wrapper.vm.modalTitle).toBe("Create Marketplace Card");
    });

    it('Submit button text is "Create Card"', () => {
      expect(wrapper.vm.submitText).toBe("Create Card");
    });

  });

  describe('Form is being used to modify a marketplace card', () => {

    const previousCard: MarketplaceCard = {
      id: 1,
      creator: makeTestUser(1),
      section: "ForSale",
      created: "2021-03-03",
      lastRenewed: "2021-03-03",
      title: "Card title",
      description: "Card description",
      keywords: [{
        id: 39,
        name: "Keyword name",
        created: "2021-01-01",
      }]
    };

    beforeEach(() => {
      const vuetify = new Vuetify();
      localVue.use(Vuex);
      // Creating wrapper around MarketplaceCardForm with data-app to appease vuetify
      const App = localVue.component('App', {
        components: { MarketplaceCardForm },
        template: '<div data-app><MarketplaceCardForm :user="user" :previousCard="previousCard"/></div>',
      });

      // Put the MarketplaceCardForm component inside a div in the global document,
      // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
      const elem = document.createElement('div');
      document.body.appendChild(elem);

      searchKeywords.mockResolvedValue([]);
      // We have to mock the $router.go method to prevent errors.
      appWrapper = mount(App, {
        stubs: ['router-link', 'router-view'],
        mocks: {
          $router: {
            go: () => {return;},
          }
        },
        localVue,
        vuetify,
        attachTo: elem,
        data() {
          return {
            user: makeTestUser(1),
            previousCard: previousCard,
          };
        }
      });

      wrapper = appWrapper.getComponent(MarketplaceCardForm);
      expect(wrapper.vm.isCreate).toBeFalsy();
    });

    it('Fields are initially populated with values of previous card', () => {
      expect(wrapper.vm.title).toBe(previousCard.title);
      expect(wrapper.vm.description).toBe(previousCard.description);
      expect(wrapper.vm.selectedSection).toBe(previousCard.section);
      expect(wrapper.vm.selectedKeywords).toStrictEqual([39]);
    });

    it('Modal title is "Edit Marketplace Card"', () => {
      expect(wrapper.vm.modalTitle).toBe("Modify Marketplace Card");
    });

    it('Submit button text is "Save Card"', () => {
      expect(wrapper.vm.submitText).toBe("Save Card");
    });

  });
});