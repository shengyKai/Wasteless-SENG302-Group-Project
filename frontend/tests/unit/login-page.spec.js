import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount } from '@vue/test-utils';
import VueRouter from 'vue-router';

Vue.use(Vuetify);
Vue.use(VueRouter);

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
    //mount Index because Index is the file in charged of toggling between pages, and we need both Login and Register
    //components to test the link, so its a full mount(not shallow mount)
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

    //the disabled value does not change immediately as it will check the form first, then change the value
    //so a small timeout is set, and then the value for disabled will be checked
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeFalsy();
    }, 100);

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
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //wrong email format, with no "@"
    await emailInput.setValue('someemail');
    await passwordInput.setValue('hello123');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //wrong email format, with no characters before "@"
    await emailInput.setValue('@gmail.com');
    await passwordInput.setValue('hello123');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //wrong email format, with no "."
    await emailInput.setValue('fsefsgr@gmailcom');
    await passwordInput.setValue('hello123');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //empty email field
    await emailInput.setValue('');
    await passwordInput.setValue('hello123');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //Too many characters for email field
    // await emailInput.setValue(
    //   'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
    // );
    // await passwordInput.setValue('hello123');
    // setTimeout(function(){
    //   expect(loginButton.props().disabled).toBeTruthy();
    // }, 100);

    //wrong password format, with no numbers
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('hello');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //wrong password format, with no alphabets
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('123455678');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //wrong password format, with less than 7 characters
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('abcd1');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

    //empty password field
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('');
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeTruthy();
    }, 100);

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

//not sure how to insert vuerouter
describe('Login.vue', () => {
  const localVue = createLocalVue();
  let vuetify;
  let vueRouter;
  beforeEach(() => {
    vuetify = new Vuetify();
  });
  it("Testing out the log in page button, should redirect to Profile Page from Login Page", async () => {
    const wrapper = mount(Login, {
      localVue,
      vuetify,
      data() {
        return {
          //override values for email and password, so that button is enabled right away
          email: "someemail@gmail.com",
          password: "hello123"
        };
      },
      components: {
        UserProfile
      },
      mocks: {
        $router
      }

    });

    const loginButton = wrapper.find(".v-btn");
    setTimeout(function(){
      expect(loginButton.props().disabled).toBeFalsy();
    }, 100);
    await loginButton.trigger('click');


    //
    // //find the link which toggles between pages
    // const link = wrapper.findComponent(Login).find('.link');
    // //click on the link to change the login value
    // await link.trigger('click');
    //
    // //if login is false, the Register component should exist and the Login component should not exist
    // expect(wrapper.findComponent(Login).exists()).toBeFalsy();
    // expect(wrapper.findComponent(Register).exists()).toBeTruthy();
  });
});