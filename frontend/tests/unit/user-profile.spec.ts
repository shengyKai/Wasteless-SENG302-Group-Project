import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { CombinedVueInstance } from 'vue/types/vue';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import { getStore, resetStoreForTesting, StoreData } from '@/store';
import UserProfile from '@/components/UserProfile.vue';

import * as api from '@/api';
import { castMock, flushQueue } from './utils';
import { User } from '@/api';

Vue.use(Vuetify);

jest.mock('@/api', () => ({
  makeBusinessAdmin: jest.fn(),
  getBusiness: jest.fn(),
  getUser: jest.fn(),
}));

const makeBusinessAdmin = castMock(api.makeBusinessAdmin);
const getBusiness = castMock(api.getBusiness);
const getUser = castMock(api.getUser);

const localVue = createLocalVue();

localVue.use(VueRouter);
const router = new VueRouter();


describe('UserProfile.vue', () => {
  // Container for the UserProfile under test
  let wrapper: Wrapper<any>;
  let store: Store<StoreData>;

  /**
   * Sets up the test UserProfile instance and populates it with test data.
   */
  beforeEach(() => {
    localVue.use(Vuex);
    resetStoreForTesting();
    store = getStore();
    store.state.user = {
      id: 1,
      firstName: "test_first_name",
      lastName: "test_last_name",
      middleName: "test_middle_name",
      nickname: "test_nickname",
      bio: "test_biography",
      email: "test_email_address",
      dateOfBirth: "1/1/1900",
      phoneNumber: "test_phone_number",
      homeAddress: { country: 'test_country' },
      created: "1/1/1950",
      role: "user",
      businessesAdministered: [
        {
          id: 1,
          name: 'test_business_name1',
          address: 'test_business_address1',
          businessType: 'Accommodation and Food Services',
        },
        { 
          id: 2,
          name: 'test_business_name2',
          address: 'test_business_address2',
          businessType: 'Accommodation and Food Services',
        }],
    };

    const vuetify = new Vuetify();

    wrapper = mount(UserProfile, {
      localVue,
      router,
      vuetify,
      store
    });

    getBusiness.mockImplementation(async businessId => {
      return {
        id: businessId,
        name: 'test_business_name' + businessId,
        address: 'test_business_address' + businessId,
        businessType: 'Accommodation and Food Services',
        administrators: [await getUser(businessId) as User],
      };
    });

    getUser.mockImplementation(async userId => {
      return {
        id:  userId,
        firstName: 'test_firstname' + userId,
        lastName: 'test_lastname' + userId,
        email: 'test_email' + userId,
        dateOfBirth: '1/1/1900',
        homeAddress: { country: 'test_user_country' + userId },
      };
    });
  });

  function actAsBusiness(businessId: number) {
    store.state.activeRole = {
      type: 'business',
      id: businessId,
    };
  }

  /**
   * Tests that the UserProfile has the user's first name somewhere in the page
   */
  it('Renders first name', () => {
    expect(wrapper.text()).toContain('test_first_name');
  });

  /**
   * Tests that the UserProfile has the user's last name somewhere in the page
   */
  it('Renders last name', () => {
    expect(wrapper.text()).toContain('test_last_name');
  });

  /**
   * Tests that the UserProfile has the user's nickname somewhere in the page
   */
  it('Renders nickname', () => {
    expect(wrapper.text()).toContain('test_nickname');
  });

  /**
   * Tests that the UserProfile has the user's biography somewhere in the page
   */
  it('Renders bio', () => {
    expect(wrapper.text()).toContain('test_biography');
  });

  /**
   * Tests that the UserProfile has the user's email somewhere in the page
   */
  it('Renders email', () => {
    expect(wrapper.text()).toContain('test_email_address');
  });

  /**
   * Tests that the UserProfile has the user's phone number somewhere in the page
   */
  it('Renders phone number', () => {
    expect(wrapper.text()).toContain('test_phone_number');
  });

  /**
   * Tests that the UserProfile has the user's home address country somewhere in the page
   */
  it('Renders home address', () => {
    expect(wrapper.text()).toContain('test_country');
  });

  /**
   * Tests that the UserProfile has the user's birthday somewhere in the page
   * and that the birthday is in the correct format dd/mm/yyyy.
   */
  // TODO Fix this test
  it.skip('Renders birthday', () => {
    expect(wrapper.text()).toContain('1/1/1900');
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
    expect(wrapper.text()).toContain('test_business_name1');
    expect(wrapper.text()).toContain('test_business_name2');
  });

  /**
   * Tests that if the user is acting as a business they administer then there should be an
   * "add admin" button.
   */
  it('If acting as a business then there should be a add admin button', async () => {
    actAsBusiness(10);

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
    actAsBusiness(10);

    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    expect(addAdmin.props().disabled).toBeFalsy();
  });

  /**
   * Tests that the "add admin" button is disabled when the user is already an administrator of the
   * current business
   */
  it('If already an administrator of the active business then the "add admin" button should be disabled', async () => {
    actAsBusiness(1); // Business 1 has an administrator with userId = 1

    await flushQueue();

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    expect(addAdmin.props().disabled).toBeTruthy();
  });

  /**
   * Tests the case where making a user an administrator of the current business succeeds.
   */
  it('If the "add admin" button is clicked then the "makeBusinessAdmin" function should be called', async () => {
    actAsBusiness(10);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation is successful
    makeBusinessAdmin.mockResolvedValue(undefined);

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    addAdmin.trigger('click');

    await flushQueue();

    expect(makeBusinessAdmin).lastCalledWith(10, 1); // Must be called with bussinessId, userId
    expect(store.state.globalError).toBeNull();
  });

  /**
   * Tests the case where making a user an administrator of the current business fails.
   */
  it('If "makeBusinessAdmin" function results in an error then this error should be shown', async () => {
    actAsBusiness(10);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation fails
    makeBusinessAdmin.mockResolvedValue('test_error_message');

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    addAdmin.trigger('click');

    await flushQueue();

    expect(makeBusinessAdmin).lastCalledWith(10, 1); // Must be called with bussinessId, userId
    expect(store.state.globalError).toBe('test_error_message');
  });

  /**
   * Tests that when the user is made an administrator then the add admin button is disabled.
   */
  it('If the user is made an admin then the "add admin" button should be disabled', async () => {
    actAsBusiness(10);

    await flushQueue();

    // Ensure that the "makeBusinessAdmin" operation is successful
    makeBusinessAdmin.mockResolvedValue(undefined);

    let addAdmin = wrapper.findComponent({ ref: 'addAdminButton' });
    addAdmin.trigger('click');

    await flushQueue();

    expect(addAdmin.props().disabled).toBeTruthy();
  });
});
