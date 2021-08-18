import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';

import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ModifyUserPage from '@/components/UserProfile/ModifyUserPage.vue';
import { getStore, resetStoreForTesting } from '@/store';
import { Business, User } from '@/api/internal';

Vue.use(Vuetify);
Vue.use(Vuex);
/**
 * Creates a list of unique test users
 *
 * @param count Number of users to create
 * @returns List of test users
 */
 function createTestBusinesses() {
  let result: Business[] = [];

    result.push({
      id: 7,
      name: 'test_name',
      primaryAdministratorId: 1,
      businessType: "Accommodation and Food Services",
      address: { city: 'test_city', country: 'test_country'},
    });
  return result;
}

const diacritics = ['À','È','Ì','Ò','Ù','à','è','ì','ò','ù','Á','É','Í','Ó','Ú','Ý','á','é','í','ó','ú','ý','Â','Ê','Î','Ô','Û','â','ê','î','ô','û','Ã','Ñ','Õ','ã','ñ','õ','Ä','Ë','Ï','Ö','Ü','Ÿ','ä','ë','ï','ö','ü','ÿ'];


describe('ModifyUserPage.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();

  beforeEach(() => {
    resetStoreForTesting();
    let business = createTestBusinesses();
    let store = getStore();
    store.state.user = {
      id: 1,
      firstName: "some firstName",
      lastName: "some lastName",
      middleName: "some middleName",
      nickname: "some nickName",
      bio: "some bio",
      email: "some email",
      dateOfBirth: "2010-01-01",
      phoneNumber: '+64 123 321 123',
      homeAddress: {
        streetNumber: '11',
        streetName: 'Test lane',
        country: "some country"
      },
      businessesAdministered: business,
    };
    let vuetify = new Vuetify();
    wrapper = mount(ModifyUserPage, {
      stubs: ['router-link', 'router-view'],
      localVue,
      vuetify,
      store,
      mocks: {
        $route: {
          params: {
            id: 1,
          }
        }
      },
    });
  }); 

  it('Street number and name should be joined together when prefilled', () => {
    expect(wrapper.vm.streetAddress).toBe('11 Test lane');
  });

  it('Country should be prefilled', () => {
    expect(wrapper.vm.user.email).toBe("some country");
  });

  it('Street number and name should be updated when the combined field is modified', async () => {
    await wrapper.setData({
      streetAddress: '13 Other place',
    });
    await Vue.nextTick();
    expect(wrapper.vm.user.homeAddress.streetNumber).toBe('13');
    expect(wrapper.vm.user.homeAddress.streetName).toBe('Other place');
  });

  it('Street number and name should be updated when the combined field is modified', async () => {
    await wrapper.setData({
      streetAddress: '13 Other place',
    });
    await Vue.nextTick();
    expect(wrapper.vm.user.homeAddress.streetNumber).toBe('13');
    expect(wrapper.vm.user.homeAddress.streetName).toBe('Other place');
  });

  it('Phone number should be split apart when prefilled', () => {
    expect(wrapper.vm.countryCode).toBe('+64');
    expect(wrapper.vm.phoneDigits).toBe('123 321 123');
  });

  it('Phone number should be joined together when updated', async () => {
    await wrapper.setData({
      countryCode: '+65',
      phoneDigits: '111',
    });
    await Vue.nextTick();
    expect(wrapper.vm.user.phoneNumber).toBe('+65 111');
  });

  it("Testing out all inputs, such that the user can only press the update button " +
  "after inputting valid formats for all fields", () => {
  const updateButton = wrapper.find(".v-btn");
  expect(updateButton.props().disabled).toBeFalsy();
});

  // it.only("Testing for invalid email format,with no '@'", async () => {
  //   const updateButton = wrapper.find(".v-btn");
  //   console.log(wrapper.vm.user.email);
  //   await wrapper.setData({
  //     email: "someemail.com"
  //   });
  //   await Vue.nextTick();
  //   expect(updateButton.props().disabled).toBeTruthy();
  // });

});