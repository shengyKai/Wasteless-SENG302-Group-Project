import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';

import Index from '@/components/Auth/index.vue';
import Login from '@/components/Auth/Login.vue';
import Register from '@/components/Auth/Register.vue';
import LocationAutocomplete from "@/components/utils/LocationAutocomplete.vue";

Vue.use(Vuetify);

/**
 * Index is used here as the mount because the tests in here focuses on the switching between Register and Login,
 * of which, Index is the parent component of both.
 */
describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;
  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Index, {
      localVue,
      vuetify,
      components: {
        Login,
        Register
      },
      data() {
        return {
          //override value for login to false, meaning the current page is at Register page
          login: false
        };
      }
    } as any);
  });

  it("Testing out the register page link, should redirect to Login Page from Register Page", async () => {
    //if login is false, the Register component should exist and the Login component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeFalsy();
    expect(wrapper.findComponent(Register).exists()).toBeTruthy();

    //find the link which toggles between pages
    const link = wrapper.findComponent(Register).find('.link');
    //click on the link to change the login value
    await link.trigger('click');

    //if login is true, the Login component should exist and the Register component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();
    expect(wrapper.findComponent(Register).exists()).toBeFalsy();
  });
});

/**
 * Tests in here focuses on the input of the fields, of which, Register would be used as the mount as it is the parent
 * of these fields.
 */
// TODO Fix this test
describe.skip('Register.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;

  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Register, {
      localVue,
      vuetify,
      components: {
        LocationAutocomplete
      },
      data() {
        //automatically fill in all details before each test except the autocomplete ones as the autocomplete fields
        //need to be filled in manually
        return {
          showPassword: false,
          showConfirmPassword: false,
          valid: false,
          email: 'someemail@gmail.com',
          password: 'somepassword1',
          confirmPassword: 'somepassword1',
          name: 'some name',
          nickname: 'some nickname',
          bio: 'some bio',
          dob: '2008-04-06',
          countryCode: '64',
          phone: '1234567890',
          street1: 'some street address',
          street2: 'some street address',
          state: "some state",
          city: "some city",
          country: "some country",
          district: "some district",
          postcode: '1234'
        };
      }
    } as any);
    //The jsdom test runner doesn't declare the fetch function, hence we need to implement it
    //ourselves to make LocationAutocomplete not crash.
    globalThis.fetch = async () => {
      return {
        json() {
          return {
            features: [],
          };
        }
      } as any;
    };
  });

  /**
   * Test to check that the register button is not disabled, because the preset data from above is all valid.
   */
  it("Testing out all inputs, such that the user can only press the register button " +
    "after inputting valid formats for all fields", () => {
    //find the register button by the component
    const registerButton = wrapper.find(".v-btn");
    //since the fields are all inputted with valid formats and all mandatory fields are filled, the button should not be
    //disabled.
    expect(registerButton.props().disabled).toBeFalsy();
  });

  /**
   * The series of tests here is to check if the register button is disabled when an INVALID format is provided in any of the
   * input fields. The fields checked are email, password, confirmPassword, name, nickname, bio, dob, countryCode, phone, street1, district, city, state,
   * country and postcode.
   */
  it("Testing for invalid email format, with less than two characters after each '.'", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      email: "someemail@gmail.c"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format,with no '@'", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      email: "someemail.com"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, with no characters before '@'", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      email: "@gmail.com"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, with no '.'", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      email: "fsefsgr@gmailcom"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, empty email field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      email: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      email: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, with no numbers", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      password: "hello"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, with no alphabets", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      password: "123455678"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, with less than 7 characters", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      password: "abcd1"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, empty password field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      password: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      password: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid confirm password format, different input value from password field", async () => {
    const registerButton = wrapper.find(".v-btn");

    //password field is "somepassword1"
    await wrapper.setData({
      confirmPassword: "somepassword2"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid confirm password format, empty confirm password field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      confirmPassword: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid name format, empty name field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      name: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid name format, name with numbers", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      name: "somename1"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid name format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      name: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid nickname format, name with numbers", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      nickname: "somename1"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid nickname format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      nickname: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid bio format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      bio: 'a'.repeat(201)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid date format, empty date field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      dob: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid country code format, no country code", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      countryCode: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid country code format, alphabets in country code", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      countryCode: "a1"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid country code format, more than three numbers in country code", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      countryCode: "1234"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid country code format, less than two numbers in country code", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      countryCode: "1"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid phone format, alphabets in field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      phone: "123456789a"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid phone format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      phone: '1'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid phone format, spaces at the start of the phone number", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      phone: " 1234567890"
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid phone format, spaces at the end of the phone number", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      phone: "1234567890 "
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid street format, empty street field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      street1: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid district format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      district: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid city format, empty city field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      city: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid city format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      city: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid state format, empty state field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      state: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid state format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      state: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid country format, empty country field", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      country: ""
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid country format, over character limit", async () => {
    const registerButton = wrapper.find(".v-btn");

    await wrapper.setData({
      country: 'a'.repeat(101)
    });
    Vue.nextTick(() => {
      expect(registerButton.props().disabled).toBeTruthy();
    });
  });

  /**
  * Tests the show password icon works if the user clicks on it
  */
  it("Testing the password field's mdi-eye icon to show user input", () => {
    //originally it should be false
    expect(wrapper.vm.showPassword).toBeFalsy();

    let showPasswordInput = wrapper.findComponent({ ref: "password" });
    let eyeButton = showPasswordInput.findComponent({ name: "v-icon" });
    //clicking on the icon would allow the user to see the password, thus making showPassword true
    eyeButton.trigger("click");
    Vue.nextTick(() => {
      expect(wrapper.vm.showPassword).toBeTruthy();
    });
  });

  /**
  * Tests the show confirm password icon works if the user clicks on it
  */
  it("Testing the confirm password field's mdi-eye icon to show user input", () => {
    //originally it should be false
    expect(wrapper.vm.showConfirmPassword).toBeFalsy();

    let showConfirmPasswordInput = wrapper.findComponent({ ref: "confirmPassword" });
    let eyeButton = showConfirmPasswordInput.findComponent({ name: "v-icon" });
    //clicking on the icon would allow the user to see the password, thus making showConfirmPassword true
    eyeButton.trigger("click");
    Vue.nextTick(() => {
      expect(wrapper.vm.showConfirmPassword).toBeTruthy();
    });
  });
});