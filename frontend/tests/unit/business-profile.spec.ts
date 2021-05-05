import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper, RouterLinkStub} from '@vue/test-utils';
import BusinessProfile from '@/components/BusinessProfile/index.vue';
import VueRouter from "vue-router";
import convertAddressToReadableText from '@/components/utils/Methods/convertJsonAddressToReadableText';

Vue.use(Vuetify);

describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  let date = new Date();

  /**
   * Set up to test the routing and whether the business profile page shows what is required
   */
  beforeEach(() => {
    const localVue = createLocalVue();
    const router = new VueRouter();
    localVue.use(VueRouter);
    vuetify = new Vuetify();
    wrapper = mount(BusinessProfile, {
      //creates a stand in(mocking) for the routerlink
      stubs: {
        RouterLink: RouterLinkStub
      },
      router,
      localVue,
      vuetify,
      //Sets up each test case with some values to ensure the business profile page works as intended
      data() {
        return {
          business: {
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
      }
    });
  });

  /**
   * Tests that the same business name exists as per the set data above
   */
  it("Must contain the business name", () => {
    expect(wrapper.text()).toContain('Some Business Name');
  });

  /**
   * Tests that the same business street address exists as per the set data above
   */
  it("Must contain the business street address", () => {
    expect(wrapper.text()).toContain('1 Some Street Name');
  });

  /**
   * Tests that the same business type exists as per the set data above
   */
  it("Must contain the business type", () => {
    expect(wrapper.text()).toContain('Some Business Type');
  });

  /**
   * Tests that the same business description exists as per the set data above
   */
  it("Must contain the business description", () => {
    expect(wrapper.text()).toContain('Some Description');
  });

  /**
   * Tests that the created date exists as per the set data above, the date set above is the current date today, and since
   * the number of months passed is rounded up to the nearest integer, it will always be (1 month ago)
   */
  it("Must contain the business created date", () => {
    expect(wrapper.text()).toContain(`${("0" + date.getDate()).slice(-2)} ` +
    `${date.toLocaleString('default', {month: 'short'})} ${date.getFullYear()} (1 months ago)`);
  });

  /**
   * Tests that the same business administrator's first name and last name exists as per the set data above
   */
  it("Must contain the business administrator first name and last name", () => {
    expect(wrapper.text()).toContain('Some First Name Some Last Name');
  });

  /**
   * Tests that another business administrator's first name and last name exists as per the set data above,
   * this is to ensure the page can show more than one administrator for one business
   */
  it("Can contain multiple business administrators", () => {
    expect(wrapper.text()).toContain('Another First Name Another Last Name');
  });

  /**
   * Tests that the business administrator link chip links to the correct endpoint
   */
  it("Router link must lead to the proper endpoint with the admin id", () => {
    expect(wrapper.findAllComponents(RouterLinkStub).at(0).props().to).toBe('/profile/1');
  });

  /**
   * Tests that the other business administrator link chip links to the correct endpoint
   */
  it("Router link can have multiple endpoints with different admin id", () => {
    expect(wrapper.findAllComponents(RouterLinkStub).at(1).props().to).toBe('/profile/2');
  });
});