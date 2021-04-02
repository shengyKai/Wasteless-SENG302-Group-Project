import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper, RouterLinkStub} from '@vue/test-utils';
import BusinessProfile from '@/components/BusinessProfile/index.vue';
import VueRouter from "vue-router";

Vue.use(Vuetify);

describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  let date = new Date();

  beforeEach(() => {
    const localVue = createLocalVue();
    const router = new VueRouter();
    localVue.use(VueRouter);
    vuetify = new Vuetify();
    wrapper = mount(BusinessProfile, {
      stubs: {
        RouterLink: RouterLinkStub
      },
      router,
      localVue,
      vuetify,
      data() {
        return {
          business: {
            name: "Some Business Name",
            address: "1 Some Street Name",
            businessType: "Some Business Type",
            description: "Some Description",
            created: date
          },
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
        };
      }
    });
  });

  it("Must contain the business name", async() => {
    expect(wrapper.text()).toContain('Some Business Name');
  });

  it("Must contain the business street address", async() => {
    expect(wrapper.text()).toContain('1 Some Street Name');
  });

  it("Must contain the business type", async() => {
    expect(wrapper.text()).toContain('Some Business Type');
  });

  it("Must contain the business description", async() => {
    expect(wrapper.text()).toContain('Some Description');
  });

  it("Must contain the business created date", async() => {
    expect(wrapper.text()).toContain(`${("0" + date.getDate()).slice(-2)} ` +
    `${date.toLocaleString('default', {month: 'short'})} ${date.getFullYear()} (1 months ago)`);
  });

  it("Must contain the business administrator first name and last name", async() => {
    expect(wrapper.text()).toContain('Some First Name Some Last Name');
  });

  it("Can contain multiple business administrators", async() => {
    expect(wrapper.text()).toContain('Another First Name Another Last Name');
  });

  it("Router link must lead to the proper endpoint with the admin id", async() => {
    expect(wrapper.findAllComponents(RouterLinkStub).at(0).props().to).toBe('/profile/1');
  });

  it("Router link can have multiple endpoints with different admin id", async() => {
    expect(wrapper.findAllComponents(RouterLinkStub).at(1).props().to).toBe('/profile/2');
  });
});