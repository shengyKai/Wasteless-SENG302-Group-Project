import Vue from 'vue';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import {createLocalVue, mount, Wrapper, RouterLinkStub} from '@vue/test-utils';
import BusinessProfile from '@/components/BusinessProfile/index.vue';
import VueRouter from "vue-router";
import * as api from '@/api/internal';
Vue.use(Vuetify);
Vue.use(Vuex);
import { getStore, resetStoreForTesting, StoreData } from '@/store';

jest.mock('@/api/internal', () => ({
  makeBusinessImagePrimary: jest.fn(),
}));

describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  let date = new Date();
  let store: Store<StoreData>;


  const user = {
    id: 1,
    firstName: 'Joe',
    lastName: 'Bloggs',
    middleName: 'ahh',
    nickname: 'Doe',
    bio: 'Bio',
    email: 'abc@123.com',
    dateOfBirth: '02-02-1943',
    phoneNumber: '',
    homeAddress: {country:"Spain"},
    created: undefined,
    role: "globalApplicationAdmin",
    businessesAdministered: [],
  };

  /**
   * Set up to test the routing and whether the business profile page shows what is required
   */
  beforeEach(() => {
    const localVue = createLocalVue();
    const router = new VueRouter();
    resetStoreForTesting();
    store = getStore();
    store.state.activeRole = {id: 1, type: 'business'};
    // @ts-ignore
    store.state.user = user;
    localVue.use(VueRouter);
    vuetify = new Vuetify();
    wrapper = mount(BusinessProfile, {
      //creates a stand in(mocking) for the routerlink
      stubs: {
        RouterLink: RouterLinkStub,
      },
      router,
      localVue,
      vuetify,
      store,
      //Sets up each test case with some values to ensure the business profile page works as intended
      data() {
        return {
          business: {
            id: 1,
            name: "Some Business Name",
            address: {
              "country": "Some Country",
              "streetName": "Some Street Name",
              "streetNumber": "1",
              "city": "Some City",
              "district": "Some District",
              "postcode": "1234",
              "region": "Some Region"
            },
            businessType: "Some Business Type",
            description: "Some Description",
            created: date,
            images: [{id:1,filename:'coolImage.jpg', thumbnailFilename:undefined}],
            administrators: [
              {
                id: 1,
                firstName: "Some First Name",
                lastName: "Some Last Name"
              },
              {
                id: 2,
                firstName: "Another First Name",
                lastName: "Another Last Name"
              }
            ]
          },
          readableAddress: "1 Some Street Name",
        };
      },
    });
  });
  it("Must contain the business name", () => {
    expect(wrapper.text()).toContain('Some Business Name');
  });

  it("Must contain the business street address", () => {
    expect(wrapper.text()).toContain('1 Some Street Name');
  });

  it("Must contain the business type", () => {
    expect(wrapper.text()).toContain('Some Business Type');
  });

  it("Must contain the business description", () => {
    expect(wrapper.text()).toContain('Some Description');
  });

  it("Must contain the business created date", () => {
    expect(wrapper.text()).toContain(`${("0" + date.getDate()).slice(-2)} ` +
    `${date.toLocaleString('default', {month: 'short'})} ${date.getFullYear()} (0 months ago)`);
  });

  it("Must contain the business administrator first name and last name", () => {
    expect(wrapper.text()).toContain('Some First Name Some Last Name');
  });

  it("Can contain multiple business administrators", () => {
    expect(wrapper.text()).toContain('Another First Name Another Last Name');
  });

  it("Router link must lead to the proper endpoint with the admin id", () => {
    expect(wrapper.findAllComponents(RouterLinkStub).at(0).props().to).toBe('/profile/1');
  });

  it("Router link can have multiple endpoints with different admin id", () => {
    expect(wrapper.findAllComponents(RouterLinkStub).at(1).props().to).toBe('/profile/2');
  });
});