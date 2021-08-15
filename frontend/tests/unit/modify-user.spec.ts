import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';

import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ModifyUserPage from '@/components/UserProfile/ModifyUserPage.vue';
import { getStore, resetStoreForTesting } from '@/store';

Vue.use(Vuetify);
Vue.use(Vuex);

describe('ModifyUserPage.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();

  beforeEach(() => {
    resetStoreForTesting();
    let store = getStore();
    store.state.user = {
      id: 1,
      firstName: "some firstName",
      lastName: "some lastName",
      email: "some email",
      homeAddress: {
        streetNumber: '11',
        streetName: 'Test lane',
        country: "some country"
      },
      phoneNumber: '+64 123 321 123'
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
});