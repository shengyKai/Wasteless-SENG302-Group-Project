import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex, { Store, StoreOptions } from 'vuex';

import { createOptions, StoreData } from '@/store';
import ProfilePage from '@/components/ProfilePage.vue';
import { CombinedVueInstance } from 'vue/types/vue';

const localVue = createLocalVue();


localVue.use(VueRouter);
const router = new VueRouter();

localVue.use(Vuetify);

const vuetify = new Vuetify({
  theme: {
    themes: {
      light: {
        primary: '#558b2f'
      }
    }
  }
});


describe('ProfilePage.vue', () => {
  let wrapper: Wrapper<CombinedVueInstance<ProfilePage, object, object, object, Record<never, any>>>;

  beforeEach(() => {
    localVue.use(Vuex);
    let options = createOptions();
    options.state = {
      user: {
        id: 1,
        firstName: "firstname",
        lastName: "lastname",
        middleName: "middlename",
        nickname: "nickname",
        bio: "biography",
        email: "email_address",
        dateOfBirth: "1/1/1900",
        phoneNumber: "phone_number",
        homeAddress: "home_address",
        created: "1/1/1950",
        role: "user",
        businessesAdministered: [],
      }
    };
    let store = new Vuex.Store(options);

    wrapper = shallowMount(ProfilePage, {
      localVue,
      router,
      vuetify,
      store
    });
  });

  it('Renders firstname', () => {
    expect(wrapper.text()).toContain('firstname');
  });
  it('Renders lastname', () => {
    expect(wrapper.text()).toContain('lastname');
  });
  it('Renders nickname', () => {
    expect(wrapper.text()).toContain('nickname');
  });
  it('Renders bio', () => {
    expect(wrapper.text()).toContain('biography');
  });
  it('Renders email', () => {
    expect(wrapper.text()).toContain('email_address');
  });
  it('Renders phone number', () => {
    expect(wrapper.text()).toContain('phone_number');
  });
  it('Renders home address', () => {
    expect(wrapper.text()).toContain('home_address');
  });

});
