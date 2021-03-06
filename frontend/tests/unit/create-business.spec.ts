import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import CreateBusiness from '@/components/BusinessProfile/CreateBusiness.vue';
import {castMock, makeTestUser, findButtonWithText, TEST_DIACRITICS} from "./utils";
import {getStore, resetStoreForTesting} from '@/store';
import {createBusiness as createBusiness1} from "@/api/business";


jest.mock('@/api/business', () => ({
  createBusiness: jest.fn(),
}));

const createBusiness = castMock(createBusiness1);
Vue.use(Vuetify);
const localVue = createLocalVue();

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
      streetAddress: '1 Elizabeth Street',
      city: 'City',
      region: 'Region',
      country: 'Country',
      postcode: '1234',
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

  it('Invalid if description too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'a'.repeat(201),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if business name not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      business: '',
    });
    await Vue.nextTick(() => {
      expect(wrapper.vm.valid).toBeFalsy();
    });
  });

  it('Invalid if business name too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      business: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if business type not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessType: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if street line 1 not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Valid when street contains the diacritic characters', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '5 ' + "????????????????????????????????????????" + ' Street',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Invalid if district too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      district: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if city not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if city too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if region not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if region too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if country not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if country too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if postcode not provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: '',
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if postcode too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: 'a'.repeat(101),
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Test that when the close button is pressed, then the "closeDialog" event should be emitted', async () => {
    await findCloseButton().trigger('click');
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

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
        streetNumber: '1',
        streetName: 'Elizabeth Street',
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
