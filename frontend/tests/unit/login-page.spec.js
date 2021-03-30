import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount } from '@vue/test-utils';
import VueRouter from 'vue-router';

Vue.use(Vuetify);

import Index from '@/components/Auth/index.vue';
import Login from '@/components/Auth/Login.vue';
import Register from '@/components/Auth/Register.vue';
import UserProfile from '@/components/UserProfile.vue';

//This test is to test the page toggling from login to register, but because index.vue has the parent component for
//the Login component, and is in charged of the toggling, it is used as the mounting point
describe('index.vue', () => {
  const localVue = createLocalVue();
  let vuetify;
  beforeEach(() => {
    vuetify = new Vuetify();
  });
  it("Testing out the log in page link, should redirect to Register Page from Login Page", async () => {
    const wrapper = mount(Index, {
      localVue,
      vuetify,
      data() {
        return {
          //override value for login to true, meaning the current page is at Login page
          login: true
        };
      },
      components: {
        Login,
        Register
      }
    });
    //if login is true, the Login component should exist and the Register component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();
    expect(wrapper.findComponent(Register).exists()).toBeFalsy();

    //find the link which toggles between pages
    const link = wrapper.findComponent(Login).find('.link');
    //click on the link to change the login value
    await link.trigger('click');

    //if login is false, the Register component should exist and the Login component should not exist
    expect(wrapper.findComponent(Login).exists()).toBeFalsy();
    expect(wrapper.findComponent(Register).exists()).toBeTruthy();
  });
});

describe('Login.vue', () => {
  const localVue = createLocalVue();
  let vuetify;

  beforeEach(() => {
    vuetify = new Vuetify();
  });
  it("Testing out the inputs for the email and password, such that the user can only press the login button " +
    "after inputting valid formats for both fields", async () => {
    const wrapper = mount(Login, {
      localVue,
      vuetify
    });

    //find the login button by the component
    const loginButton = wrapper.find(".v-btn");
    //initial value of disabled should be true, since both fields are empty
    expect(loginButton.props().disabled).toBeTruthy();

    //find the email input by the type
    const emailInput = wrapper.find('input[type="email"]');
    //set a valid input for the email
    await emailInput.setValue('someemail@gmail.com');

    //find the password input by the type
    const passwordInput = wrapper.find('input[type="password"]');
    //set a valid input for the password
    await passwordInput.setValue('hello123');

    //Docs from the vue api:
    //nextTick() Defers the callback to be executed after the next DOM update cycle.
    //Use it immediately after you’ve changed some data to wait for the DOM update.
    //In this case, we just changed some data on the email and password field, so we need to call nextTick for a DOM
    //update.
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeFalsy();
    });
  });

  it("Testing out the inputs for the email and password, such that the user will be unable to press the login " +
    "button if the input is invalid", async () => {
    const wrapper = mount(Login, {
      localVue,
      vuetify
    });

    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    //wrong email format, with less than two characters after each "."
    let emailInput = wrapper.find('input[type="email"]');
    await emailInput.setValue('someemail@gmail.c');
    let passwordInput = wrapper.find('input[type="password"]');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //wrong email format, with no "@"
    await emailInput.setValue('someemail');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //wrong email format, with no characters before "@"
    await emailInput.setValue('@gmail.com');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //wrong email format, with no "."
    await emailInput.setValue('fsefsgr@gmailcom');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //empty email field
    await emailInput.setValue('');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //!!!NOTICE!!!
    // this test lags the whole application, so is commented out for the moment
    // Too many characters for email field
    // await emailInput.setValue(
    //   'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
    // );
    // await passwordInput.setValue('hello123');
    // await Vue.nextTick(() => {
    //   expect(loginButton.props().disabled).toBeTruthy();
    // });


    //wrong password format, with no numbers
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('hello');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //wrong password format, with no alphabets
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('123455678');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //wrong password format, with less than 7 characters
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('abcd1');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //empty password field
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });

    //!!!NOTICE!!!
    ///this test lags the whole application, so is commented out for the moment
    //Too many characters for password field
    // await emailInput.setValue('someemail@gmail.com');
    // await passwordInput.setValue(
    //   'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
    // );
    // setTimeout(function(){
    //   expect(loginButton.props().disabled).toBeTruthy();
    // }, 100);
  });
});


describe('index.vue', () => {
  const localVue = createLocalVue();
  let vuetify;
  localVue.use(VueRouter);
  beforeEach(() => {
    vuetify = new Vuetify();
  });
  it("Testing out the log in page button, should redirect to Profile Page from Login Page", async () => {
    const wrapper = mount(Index, {
      localVue,
      vuetify,
      components: {
        Login,
        UserProfile
      },
      data() {
        return {
          //override value for login to true, meaning the current page is at Login page
          login: true
        };
      }
    });

    //initially wrapper should not be able to find UserProfile page as its in the Login page
    //checking Login page existence
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();
    //checking Profile page existence
    expect(wrapper.findComponent(UserProfile).exists()).toBeFalsy();

    const loginPage = wrapper.findComponent(Login);
    //enter proper, valid inputs for both email and password fields
    const emailInput = loginPage.find('input[type="email"]');
    await emailInput.setValue('someemail@gmail.com');
    const passwordInput = loginPage.find('input[type="password"]');
    await passwordInput.setValue('hello123');

    const loginButton = wrapper.findComponent(Login).find(".v-btn");
    //should be disabled at first because DOM is still not updated
    expect(loginButton.props().disabled).toBeTruthy();

    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeFalsy();
    });

    await loginButton.trigger('click');

    expect(window.location.href).toBe('/profile');
  });
});
