import Vue from "vue";
import Vuex, { Store } from "vuex";
import Vuetify from "vuetify";

import { createLocalVue, mount, Wrapper } from "@vue/test-utils";

import ModifyUserPage from "@/components/UserProfile/ModifyUserPage.vue";
import { getStore, resetStoreForTesting } from "@/store";
import { Business, BUSINESS_TYPES, User } from "@/api/internal";

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
    name: "test_name",
    primaryAdministratorId: 1,
    businessType: "Accommodation and Food Services",
    address: { city: "test_city", country: "test_country" },
  });
  return result;
}

describe("ModifyUserPage.vue", () => {
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
      phoneNumber: "+64 123 321 123",
      homeAddress: {
        streetNumber: "11",
        streetName: "Test lane",
        country: "some country",
      },
      businessesAdministered: business,
    };
    let vuetify = new Vuetify();
    wrapper = mount(ModifyUserPage, {
      stubs: ["router-link", "router-view"],
      localVue,
      vuetify,
      store,
      mocks: {
        $route: {
          params: {
            id: 1,
          },
        },
      },
    });
  });
  it("Email should be prefilled", async () => {
    await wrapper.setData({
      user: {
        email: "some@email.com",
        firstName: "some firstName",
      },
    });
    expect(wrapper.vm.user.email).toBe("some@email.com");
  });

  it("First Name should be prefilled", async () => {
    await wrapper.setData({
      user: {
        firstName: "some firstName",
      },
    });
    expect(wrapper.vm.user.firstName).toBe("some firstName");
  });

  it("Last name should be prefilled", async () => {
    await wrapper.setData({
      user: {
        lastName: "some lastName",
      },
    });
    expect(wrapper.vm.user.lastName).toBe("some lastName");
  });

  it("Middle Name should be prefilled", async () => {
    await wrapper.setData({
      user: {
        middleName: "some middleName",
      },
    });
    expect(wrapper.vm.user.middleName).toBe("some middleName");
  });

  it("Nick name should be prefilled", async () => {
    await wrapper.setData({
      user: {
        nickName: "some nickName",
      },
    });
    expect(wrapper.vm.user.nickName).toBe("some nickName");
  });

  it("Bio should be prefilled", async () => {
    await wrapper.setData({
      user: {
        bio: "some bio",
      },
    });
    expect(wrapper.vm.user.bio).toBe("some bio");
  });

  it("Date of birth should be prefilled", async () => {
    await wrapper.setData({
      user: {
        dateOfBirth: "2010-01-01",
      },
    });
    expect(wrapper.vm.user.dateOfBirth).toBe("2010-01-01");
  });

  it("Street number and name should be joined together when prefilled", () => {
    expect(wrapper.vm.streetAddress).toBe("11 Test lane");
  });

  it("Country should be prefilled", () => {
    expect(wrapper.vm.user.homeAddress.country).toBe("some country");
  });

  it("Street number and name should be updated when the combined field is modified", async () => {
    await wrapper.setData({
      streetAddress: "13 Other place",
    });
    await Vue.nextTick();
    expect(wrapper.vm.user.homeAddress.streetNumber).toBe("13");
    expect(wrapper.vm.user.homeAddress.streetName).toBe("Other place");
  });

  it("Street number and name should be updated when the combined field is modified", async () => {
    await wrapper.setData({
      streetAddress: "13 Other place",
    });
    await Vue.nextTick();
    expect(wrapper.vm.user.homeAddress.streetNumber).toBe("13");
    expect(wrapper.vm.user.homeAddress.streetName).toBe("Other place");
  });

  it("Phone number should be split apart when prefilled", () => {
    expect(wrapper.vm.countryCode).toBe("+64");
    expect(wrapper.vm.phoneDigits).toBe("123 321 123");
  });

  it("Phone number should be joined together when updated", async () => {
    await wrapper.setData({
      countryCode: "+65",
      phoneDigits: "111",
    });
    await Vue.nextTick();
    expect(wrapper.vm.user.phoneNumber).toBe("+65 111");
  });

  it(
    "Testing out all inputs, such that the user can only press the update button " +
            "after inputting valid formats for all fields",
    async () => {
      const updateButton = wrapper.find(".v-btn");
      await wrapper.setData({
        user: {
          email: "someone@email.com",
          newPassword:"somepassword111",
          password: "password123"
        },
        confirmPassword: "somepassword111"
      });
      await Vue.nextTick();
      expect(updateButton.props().disabled).toBeFalsy();
    }
  );

  it("Invalid email format,with no '@'", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { email: "someemail.com" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid firstName format,with '@' ", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { firstName: "some firstName@" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid lastName format,with '@' ", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { lastName: "some lastName@" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid middleName format, with '@' ", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { middleName: "some middleName@" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid date of birth format,with unexpected char and symbol'@' ", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { dateOfBirth: "65a4sd@asd" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Testing for empty date of birth field", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { dateOfBirth: "" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid date of birth format", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { dateOfBirth: "01-01-01" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid date of birth format -- too young", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { dateOfBirth: "2020-01-01" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid date of birth format -- too old", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { dateOfBirth: "2050-01-01" },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid date of birth format, should be 13years from now when no business", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: { dateOfBirth: "2009-01-01", businessAdministered: [] },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid date of birth format should be 16years from now when have business", async () => {
    const updateButton = wrapper.find(".v-btn");
    let business = createTestBusinesses();
    await wrapper.setData({
      user: {
        dateOfBirth: "2008-01-01",
        businessAdministered: business,
      },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid phone number, missing countryCode", async () => {
    await wrapper.setData({
      countryCode: "+",
      phoneDigits: "111",
    });
    await Vue.nextTick();
    const updateButton = wrapper.find(".v-btn");
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Invalid phone number, missing phoneDigits", async () => {
    await wrapper.setData({
      countryCode: "+64",
      phoneDigits: "",
    });
    await Vue.nextTick();
    const updateButton = wrapper.find(".v-btn");
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("New password field not empty, confirmPassword is empty.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        newPassword: "asdqwe123",
      },
      confirmPassword: "",
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("New password field not empty, currentPassword is empty.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        newPassword: "asdqwe123",
        password: "",
      },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("New password field not empty, confirmPassword & currentPassword is empty.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        newPassword: "asdqwe123",
        password: "",
      },
      confirmPassword: "",
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("New password field not empty and confirmPassword dosent match.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        newPassword: "asdqwe123",
        password: "asdqwe123",
      },
      confirmPassword: "rtyrty543",
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Email field is modified and currentPassword is empty.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        email: "some@email.com",
        password: "",
      },
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeTruthy();
  });

  it("Email field is modified and currentPassword was not empty.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        email: "some@email.com",
        newPassword: "",
        password: "qweqwe123",
      },
      confirmPassword: "",
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeFalsy();
  });

  it("Email and password(s) field is modified and currentPassword was not empty.", async () => {
    const updateButton = wrapper.find(".v-btn");
    await wrapper.setData({
      user: {
        email: "some@email.com",
        newPassword: "asdasd123",
        password: "qweqwe123",
      },
      confirmPassword: "asdasd123",
    });
    await Vue.nextTick();
    expect(updateButton.props().disabled).toBeFalsy();
  });
});
