import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex from 'vuex';
import { CombinedVueInstance } from 'vue/types/vue';
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';

import { createOptions } from '@/store';
import UserProfile from '@/components/UserProfile.vue';

Vue.use(Vuetify);

const localVue = createLocalVue();

localVue.use(VueRouter);
const router = new VueRouter();


describe('UserProfile.vue', () => {
  // Container for the UserProfile under test
  let wrapper: Wrapper<any>;

  /**
   * Sets up the test UserProfile instance and populates it with test data.
   */
  beforeEach(() => {
    localVue.use(Vuex);
    let options = createOptions();
    options.state = {
      user: {
        id: 1,
        firstName: "test_first_name",
        lastName: "test_last_name",
        middleName: "test_middle_name",
        nickname: "test_nickname",
        bio: "test_biography",
        email: "test_email_address",
        dateOfBirth: "1/1/1900",
        phoneNumber: "test_phone_number",
        homeAddress: "test_home_address",
        created: "1/1/1950",
        role: "user",
        businessesAdministered: [],
      }
    };
    let store = new Vuex.Store(options);

    const vuetify = new Vuetify();

    wrapper = shallowMount(UserProfile, {
      localVue,
      router,
      vuetify,
      store
    });
  });

  /**
   * Tests that the UserProfile has the user's first name somewhere in the page
   */
  it('Renders firstname', () => {
    expect(wrapper.text()).toContain('test_first_name');
  });

  /**
   * Tests that the UserProfile has the user's last name somewhere in the page
   */
  it('Renders lastname', () => {
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
   * Tests that the UserProfile has the user's home address somewhere in the page
   */
  it('Renders home address', () => {
    expect(wrapper.text()).toContain('test_home_address');
  });
});
