import Vue from 'vue';
import Vuex, { Store } from 'vuex';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import ModifyBusiness from '@/components/BusinessProfile/index.vue';
import {castMock} from "./utils";
import * as api from '@/api/internal';
import {getStore, resetStoreForTesting} from '@/store';
import {User, Location, Business} from "@/api/internal";

jest.mock('@/api/internal', () => ({
  modifyBusiness: jest.fn(),
}));

const modifyBusiness = castMock(api.modifyBusiness);
Vue.use(Vuetify);
const localVue = createLocalVue();

/**
 * Creates a test location
 *
 * @returns The generated location
 */
function createTestLocation() {
  let location: Location = {
    streetNumber: 'test_street_number',
    streetName: 'test_streetAddress',
    city: 'test_city',
    region: 'test_region',
    postcode: 'test_postcode',
    district: 'test_district',
    country: 'test_country'
  };
  return location;
}

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @returns The generated user
 */
function createTestUser(userId: number) {
  let user: User = {
    id: userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: createTestLocation()
  };
  return user;
}

/**
 * Creates a test business with the given business id
 *
 * @param businessId The business id to use
 * @param primaryAdminId the id of the user who will be the primary administrator
 * @param admins a list of users to be the administrators of the businesses
 */
function createTestBusiness(businessId: number, primaryAdminId: number, admins: User[]) {
  let business: Business = {
    id: businessId,
    primaryAdministratorId: primaryAdminId,
    administrators: admins,
    name: 'test_businessname' + businessId,
    description: 'test_description' + businessId,
    address: createTestLocation(),
    businessType: 'Charitable organisation',
    created: '2/6/2006'
  };
  return business;
}

describe('modifyBusiness.vue', () => {
  // Container for the wrapper that goes around Modify Business
  let appWrapper: Wrapper<any>;

  // Container for the ModifyBusiness used in these tests
  let wrapper: Wrapper<any>;

  const diacritics = ['À','È','Ì','Ò','Ù','à','è','ì','ò','ù','Á','É','Í','Ó','Ú','Ý','á','é','í','ó','ú','ý','Â','Ê','Î','Ô','Û','â','ê','î','ô','û','Ã','Ñ','Õ','ã','ñ','õ','Ä','Ë','Ï','Ö','Ü','Ÿ','ä','ë','ï','ö','ü','ÿ'];

  /**
   * Sets up the test ModifyBusiness instance
   */
  beforeEach(() => {
    const vuetify = new Vuetify();
    localVue.use(Vuex);
    resetStoreForTesting();
    let store = getStore();
    let testUser = createTestUser(1);
    store.state.user = testUser;
    store.state.business = createTestBusiness(1, testUser.id, [testUser]);

    const App = localVue.component('App', {
      components: { ModifyBusiness },
      template: '<div data-app><index/></div>'
    });

    const elem = document.createElement('div');
    document.body.appendChild(elem);

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

    wrapper = appWrapper.getComponent(ModifyBusiness);
  });

  /**
   * Finds the update button in the Modify Business form
   *
   * @returns A wrapper around the update button
   */
  function findUpdateButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Update Business'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  it('Valid if no fields are provided', async () => {
    //TODO
  });

  it('Valid if all fields are provided', async () => {
    //TODO
  });

  it('Valid if the new name of the business is provided', async () => {
    //TODO
  });

  it('Valid if the new description of the business is provided', async () => {
    //TODO
  });

  it('Valid if the new business type of the business is provided', async () => {
    //TODO
  });

  it('Valid if the new street address is provided', async () => {
    //TODO
  });

  it('Valid if the new district is provided', async () => {
    //TODO
  });

  it('Valid if the new city is provided', async () => {
    //TODO
  });

  it('Valid if the new region is provided', async () => {
    //TODO
  });

  it('Valid if the new country is provided', async () => {
    //TODO
  });

  it('Valid if the new postcode is provided', async () => {
    //TODO
  });

  it('Invalid if the new business name is too long', async () => {
    //TODO
  });

  it('Invalid if the new description is too long', async () => {
    //TODO
  });

  it('Invalid if the street address contains a character', async () => {
    //TODO
  });

  it('Invalid if the street address is too long', async () => {
    //TODO
  });

  it('Invalid if the district is too long', async () => {
    //TODO
  });

  it('Invalid if the city is too long', async () => {
    //TODO
  });

  it('Invalid if the region is too long', async () => {
    //TODO
  });

  it('Invalid if the country is too long', async () => {
    //TODO
  });

  it('Invalid if postcode is too long', async () => {
    //TODO
  });
});
