import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import {createLocalVue, Wrapper, mount, createWrapper} from '@vue/test-utils';

import { getStore, resetStoreForTesting, StoreData } from '@/store';
import UserProfile from '@/components/UserProfile.vue';

import * as api from '@/api';
import { castMock, flushQueue } from './utils';
import { User, Business } from '@/api';

Vue.use(Vuetify);

jest.mock('@/api', () => ({
  makeBusinessAdmin: jest.fn(),
  removeBusinessAdmin: jest.fn(),
  getBusiness: jest.fn(),
  getUser: jest.fn(),
}));

const makeBusinessAdmin = castMock(api.makeBusinessAdmin);
const removeBusinessAdmin = castMock(api.removeBusinessAdmin);

const getBusiness = castMock(api.getBusiness);
const getUser = castMock(api.getUser);

const localVue = createLocalVue();

/**
 * Creates a test business with the given business id
 *
 * @param businessId The business id to use
 * @param administrators The administrator ids to administer this business
 * @returns The generated business
 */
function makeTestBusiness(businessId: number, administrators?: number[]) {
  let business: Business = {
    id: businessId,
    name: 'test_business_name' + businessId,
    address: 'test_business_address' + businessId,
    description: 'test_business_description' + businessId,
    created: '1/5/2005',
    businessType: 'Accommodation and Food Services',
  };

  if (administrators !== undefined) {
    business.administrators = administrators.map(userId => makeTestUser(userId));
  }
  return business;
}

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @param businesses The businesses for this user to administer
 * @param applicationAdmin True if you want the user to be an system administrator
 * @returns The generated user
 */
function makeTestUser(userId: number, businesses?: number[], applicationAdmin?: boolean) {
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
    homeAddress: { country: 'test_country' + userId },
    role: applicationAdmin ? 'globalApplicationAdmin' : 'user'
  };

  if (businesses !== undefined) {
    user.businessesAdministered = businesses.map(businessId => makeTestBusiness(businessId));
  }
  return user;
}

/**
 * Describe groups the following tests together into a single component
 * This component tests against the User Profile page
 *
 * For this set of tests the users involved are as follows.
 *  - User 1  : The user that is observing the profile. They administer businesses 1 and 2.
 *  - User 100: The user within this UserProfile. They administer businesses 100 and 101.
 */
describe('UserProfile.vue', () => {
  // Container for the wrapper around UserProfile
  let appWrapper: Wrapper<any>;
  // Container for the UserProfile under test
  let wrapper: Wrapper<any>;
  // The global store to be used
  let store: Store<StoreData>;

  /**
   * Sets up the test UserProfile instance and sets the route with id = 100.
   *
   * Because the element we're testing has a v-dialog we need to take some extra steps to make it
   * work.
   */
  beforeEach(() => {
    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = makeTestUser(1, [1, 2]);
    store.state.user.role = 'user'; // Make sure we have this private field instantiated.

    getBusiness.mockImplementation(async businessId => {
      return makeTestBusiness(businessId, [businessId, businessId - 1]);
    });

    getUser.mockImplementation(async userId => {
      if (userId === 200) {
        return makeTestUser(userId, [userId, userId + 1], true);
      }
      return makeTestUser(userId, [userId, userId + 1]);
    });

    generateWrapper(100);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the UserProfile component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Creates the environment used for testing. The profile page being viewed can be altered by changing the route parameter
   * @param route The ID of the user to view the profile of
   */
  function generateWrapper(route: number) {
    // Creating wrapper around UserProfile with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { UserProfile },
      template: '<div data-app><UserProfile/></div>',
    });

    // Put the UserProfile component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);
    const vuetify = new Vuetify();
    appWrapper = mount(App, {
      stubs: ['router-link', 'router-view'],
      mocks: {
        $route: {
          params: {
            id: route,
          }
        }
      },
      localVue,
      vuetify,
      store,
      attachTo: elem,
    });

    wrapper = appWrapper.getComponent(UserProfile);
  }

  /**
   * Makes the current viewer of the profile page act as the provided business
   * @param businessId The business to act as
   */
  function actAsBusiness(businessId: number) {
    store.state.activeRole = {
      type: 'business',
      id: businessId,
    };

    let currentUser = store.state.user!;
    if (!currentUser.businessesAdministered!.some(business => business.id === businessId)) {
      // If the current user is an admin of the business we want to act as then add ourselves as an
      // admin
      currentUser.businessesAdministered!.push(makeTestBusiness(businessId));
    }
  }

  /**
   * Tests that when the UserProfile is created that it fetches the user with "getUser" with the
   * userId from the route params.
   */
  it('Fetches user with "getUser"', () => {
    expect(getUser).toBeCalledWith(100);
  });

  /**
   * Tests that the UserProfile has the user's first name somewhere in the page
   */
  it('Renders first name', () => {
    expect(wrapper.text()).toContain('test_firstname100');
  });

  /**
   * Tests that the UserProfile has the user's last name somewhere in the page
   */
  it('Renders last name', () => {
    expect(wrapper.text()).toContain('test_lastname100');
  });

  /**
   * Tests that the UserProfile has the user's nickname somewhere in the page
   */
  it('Renders nickname', () => {
    expect(wrapper.text()).toContain('test_nickname100');
  });

  /**
   * Tests that the UserProfile has the user's biography somewhere in the page
   */
  it('Renders bio', () => {
    expect(wrapper.text()).toContain('test_biography100');
  });

  /**
   * Tests that the UserProfile has the user's email somewhere in the page
   */
  it('Renders email', () => {
    expect(wrapper.text()).toContain('test_email100');
  });

  /**
   * Tests that the UserProfile has the user's phone number somewhere in the page
   */
  it('Renders phone number', () => {
    expect(wrapper.text()).toContain('test_phone_number100');
  });

  /**
   * Tests that the UserProfile has the user's home address country somewhere in the page
   */
  it('Renders home address', () => {
    expect(wrapper.text()).toContain('test_country100');
  });

  /**
   * Tests that the UserProfile has the user's birthday somewhere in the page
   * and that the birthday is in the correct format dd MMM yyyy.
   */
  it('Renders birthday', () => {
    expect(wrapper.text()).toContain('01 Jan 1900');
  });

  /**
   * Tests that the UserProfile contains a creation message somewhere in the page
   */
  it('Renders computed creation message', () => {
    expect(wrapper.text()).toContain(wrapper.vm.createdMsg);
  });

  /**
   * Tests that the UserProfile has a creation message in the format "dd mmm yyyy (x months ago)"
   */
  it('Creation message is in valid format', () => {
    expect(wrapper.vm.createdMsg).toMatch(/[0-9]{1,2} [A-Z][a-z]{2} [0-9]+ \([0-9]+ months ago\)/);
  });

  /**
   * Tests that the UserProfile displays the user's businesses.
   */
  it('User businesses are displayed', async () => {
    await Vue.nextTick();
    expect(wrapper.text()).toContain('test_business_name100');
    expect(wrapper.text()).toContain('test_business_name101');
  });

  /**
   * Tests that if the user is acting as a business they administer then there should be an
   * "add admin" button.
   */
  it('If acting as a business then there should be a add admin button', async () => {
    actAsBusiness(1);

    await Vue.nextTick();

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    expect(addAdmin.exists()).toBeTruthy();
  });

  /**
   * Tests that if the user is acting as themselves then there should be no add admin button.
   */
  it('If not acting as a business then there should not be a add admin button', async () => {
    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    expect(addAdmin.exists()).toBeFalsy();
  });

  /**
   * Tests that if the current user is not an administrator of the current business then the
   * "add admin" button should be enabled.
   */
  it('If not an administrator of the active business then the "add admin" button should be enabled', async () => {
    actAsBusiness(1);

    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    expect(addAdmin.props().disabled).toBeFalsy();
  });

  /**
   * Tests that the "add admin" button is disabled when the user is already an administrator of the
   * current business
   */
  it('If already an administrator of the active business then the "add admin" button should be disabled', async () => {
    actAsBusiness(100); // Business 100 has an administrator with userId = 100

    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    expect(addAdmin.props().disabled).toBeTruthy();
  });

  /**
   * Tests the case where making a user an administrator of the current business succeeds.
   */
  it('If the "add admin" button is clicked then the "makeBusinessAdmin" function should be called', async () => {
    actAsBusiness(1);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation is successful
    makeBusinessAdmin.mockResolvedValue(undefined);

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    addAdmin.trigger('click');

    await flushQueue();

    let confirm = wrapper.findComponent({ref: 'confirmButton' });
    confirm.trigger('click');
    await flushQueue();

    expect(makeBusinessAdmin).lastCalledWith(1, 100); // Must be called with bussinessId, userId
    expect(store.state.globalError).toBeNull();
  });

  /**
   * Tests the case where making a user an administrator of the current business fails.
   */
  it('If "makeBusinessAdmin" function results in an error then this error should be shown', async () => {
    actAsBusiness(1);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation fails
    makeBusinessAdmin.mockResolvedValue('test_error_message');

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    addAdmin.trigger('click');
    await flushQueue();

    let confirm = wrapper.findComponent({ref: 'confirmButton' });
    confirm.trigger('click');
    await flushQueue();

    expect(makeBusinessAdmin).lastCalledWith(1, 100); // Must be called with bussinessId, userId
    expect(store.state.globalError).toBe('test_error_message');
  });

  /**
   * Tests that if the user is acting as a business they administer then there should be an
   * "remove admin" button.
   */
  it('If acting as a business there should be a "remove admin" button', async () => {
    actAsBusiness(1);

    await Vue.nextTick();

    let addAdmin = wrapper.findComponent({ ref: 'removeAdminButton' });
    expect(addAdmin.exists()).toBeTruthy();
  });

  /**
   * Tests that if the user is acting as themselves then there should be no remove admin button.
   */
  it('If not acting as a business then there should not be a remove admin button', async () => {
    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'removeAdminButton' });
    expect(addAdmin.exists()).toBeFalsy();
  });

  /**
   * Tests that if the current user is not an administrator of the current business then the
   * "remove admin" button should be disabled.
   */
  it('If not an administrator of the active business then the "remove admin" button should be disabled', async () => {
    actAsBusiness(1);

    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'removeAdminButton' });
    expect(addAdmin.props().disabled).toBeTruthy();
  });

  /**
   * Tests that the "remove admin" button is enabled when the user is an administrator of the
   * current business
   */
  it('If an administrator of the active business then the "remove admin" button should be enabled', async () => {
    actAsBusiness(100); // Business 100 has an administrator with userId = 100

    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'removeAdminButton' });
    expect(addAdmin.props().disabled).toBeFalsy();
  });

  /**
   * Tests the case where removing a user from administrator of the current business succeeds.
   */
  it('If the "remove admin" button is clicked then the "removeBusinessAdmin" function should be called', async () => {
    actAsBusiness(100);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation is successful
    removeBusinessAdmin.mockResolvedValue(undefined);

    let removeAdmin = wrapper.findComponent({ ref: 'removeAdminButton' });
    removeAdmin.trigger('click');

    await flushQueue();

    let confirm = wrapper.findComponent({ref: 'confirmButton' });
    confirm.trigger('click');
    await flushQueue();

    expect(removeBusinessAdmin).lastCalledWith(100, 100); // Must be called with bussinessId, userId
    expect(store.state.globalError).toBeNull();
  });

  /**
   * Tests the case where removing a user from administrator of the current business fails.
   */
  it('If "removeBusinessAdmin" function results in an error then this error should be shown', async () => {
    actAsBusiness(100);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation fails
    removeBusinessAdmin.mockResolvedValue('test_error_message');

    let removeAdmin = wrapper.findComponent({ ref: 'removeAdminButton' });
    removeAdmin.trigger('click');
    await flushQueue();

    let confirm = wrapper.findComponent({ref: 'confirmButton' });
    confirm.trigger('click');
    await flushQueue();

    expect(removeBusinessAdmin).lastCalledWith(100, 100); // Must be called with bussinessId, userId
    expect(store.state.globalError).toBe('test_error_message');
  });

  /**
   * Tests that the application administrator status is hidden by default
   */
  it('If not an Application admin, then there should not be a administrator chip', async () => {
    let adminChip = wrapper.findComponent({ref:'administratorStatus'});
    expect(adminChip.exists()).toBeFalsy();
  });

  /**
   * Tests that when viewing an application admin as an application admin, the application administrator status is shown
   */
  it('If acting as application admin and viewing an admin, then there should be a administrator chip', async () => {
    // To be able to see the role of another user implies you are an admin. No extra steps needed. Just mock the user role.
    generateWrapper(200);
    await flushQueue();
    let adminChip = wrapper.findComponent({ref:'administratorStatus'});
    expect(adminChip.exists()).toBeTruthy();
  });
});
