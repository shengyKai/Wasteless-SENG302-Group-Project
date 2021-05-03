import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import CreateBusiness from '@/components/BusinessProfile/CreateBusiness.vue';
import {castMock} from "./utils";
import * as api from '@/api';
import { getStore, resetStoreForTesting } from '@/store';
import {User} from "@/api";


jest.mock('@/api', () => ({
  createBusiness: jest.fn(),
}));

const createBusiness = castMock(api.createBusiness);
Vue.use(Vuetify);
const localVue = createLocalVue();

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @param businesses The businesses for this user to administer
 * @returns The generated user
 */
function makeTestUser(userId: number) {
  let user: User = {
    id:  userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: {
      streetNumber: 'test_street_number',
      streetName: 'test_street1',
      city: 'test_city',
      region: 'test_region',
      postcode: 'test_postcode',
      district: 'test_district',
      country: 'test_country' + userId
    },
    businessesAdministered: [],
  };


  return user;
}

describe('CreateBusiness.vue', () => {
  // Container for the wrapper around CreateBusiness
  let appWrapper: Wrapper<any>;

  // Container for the CreateBusiness under test
  let wrapper: Wrapper<any>;

  /**
   * Executes before all the tests.
   *
   * The jsdom environment doesn't declare the fetch function, hence we need to implement it
   * ourselves to make LocationAutocomplete not crash.
   */
  beforeAll(() => {
    globalThis.fetch = async () => {
      return {
        json() {
          return {
            features: [],
          };
        }
      } as any;
    };
  });

  /**
   * Sets up the test CreateBusiness instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();
    localVue.use(Vuex);
    resetStoreForTesting();
    let store = getStore();
    store.state.user = makeTestUser(1); // log in as user 1
    // Creating wrapper around CreateBusiness with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { CreateBusiness },
      template: '<div data-app><CreateBusiness/></div>',
    });

    // Put the CreateBusiness component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

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
      store: store,
    });

    wrapper = appWrapper.getComponent(CreateBusiness);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the CreateBusiness component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Adds all the fields that are required for the create business form to be valid
   *
   * These are:
   * - Business name
   * - Business type
   * - Street address line 1
   * - City
   * - Region
   * - Country
   * - Postcode
   */
  async function populateRequiredFields() {
    await wrapper.setData({
      business: 'Business Name',
      businessType: 'Business Type',
      street1: 'Street 1',
      city: 'City',
      region: 'Region',
      country: 'Country',
      postcode: '1234',
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
   * Tests that the CreateBusiness is valid if all required fields are provided
   */
  it('Valid if all required fields are provided', async () => {
    await populateRequiredFields();

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeTruthy();
  });

  /**
   * Tests that the CreateBusiness is invalid if description field is too long (> 200 characters)
   */
  it('Invalid if description too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'a'.repeat(201),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no business name field is provided
   */
  it('Invalid if business name not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      business: '',
    });

    await Vue.nextTick(() => {
      expect(wrapper.vm.valid).toBeFalsy();
    });
  });

  /**
   * Tests that the CreateBusiness is invalid if business name field is too long (> 100 characters)
   */
  it('Invalid if business name too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      business: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no business type field is provided
   */
  it('Invalid if business type not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessType: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no street line 1 field is provided
   */
  it('Invalid if street line 1 not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      street1: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if district field is too long (> 100 characters)
   */
  it('Invalid if district too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      district: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no city field is provided
   */
  it('Invalid if city not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if city field is too long (> 100 characters)
   */
  it('Invalid if city too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no region field is provided
   */
  it('Invalid if region not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if region field is too long (> 100 characters)
   */
  it('Invalid if region too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no country field is provided
   */
  it('Invalid if country not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if country field is too long (> 100 characters)
   */
  it('Invalid if country too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if no postcode field is provided
   */
  it('Invalid if postcode not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: '',
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that the CreateBusiness is invalid if postcode field is too long (> 100 characters)
   */
  it('Invalid if postcode too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: 'a'.repeat(101),
    });

    await Vue.nextTick();

    expect(wrapper.vm.valid).toBeFalsy();
  });

  /**
   * Tests that when the close button is pressed the "closeDialog" event is emitted, this should
   * also result in the dialog getting closed.
   */
  it('Test that when the close button is pressed, then the "closeDialog" event should be emitted', async () => {
    await findCloseButton().trigger('click');
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  /**
   * Tests that when the create button is pressed and the api call is successful that the parameters
   * are passed to the api function and the dialog is closed.
   */
  it('When the create button is pressed then an api call should be made and is successful', async () => {
    await populateRequiredFields();
    createBusiness.mockResolvedValue(undefined); // Ensure that the operation is successful

    await Vue.nextTick();

    await findCreateButton().trigger('click'); // Click create button

    await Vue.nextTick();

    expect(createBusiness).toBeCalledWith({
      primaryAdministratorId: 1,
      name: 'Business Name',
      description: '',
      businessType: 'Business Type',
      address: {
        streetNumber: 'Street',
        streetName: '1',
        district: '',
        city: 'City',
        region: 'Region',
        country: 'Country',
        postcode: '1234',
      }
    });
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });
});
