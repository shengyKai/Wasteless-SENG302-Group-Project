import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import Index from '@/components/Auth/index.vue';
import Login from '@/components/Auth/Login.vue';
import Register from '@/components/Auth/Register.vue';

Vue.use(Vuetify);

//This test is to test the page toggling from login to register, but because index.vue has the parent component for
//the Login component, and is in charged of the toggling, it is used as the mounting point
describe('index.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;
  const methodInvoked = jest.spyOn((Login as any).methods, 'login');
  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Index, {
      localVue,
      vuetify,
      methodInvoked,
      components: {
        Login,
        Register
      },
      data() {
        return {
          //override value for login to true, meaning the current page is at Login page
          login: true
        };
      }
    } as any);
  });
  it("Testing out the log in page link, should redirect to Register Page from Login Page", async () => {
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

  it("Testing out the log in page button, should call function login which redirects to Profile Page from " +
    "Login Page", async () => {
    //initially wrapper should not be able to find UserProfile page as its in the Login page
    //checking Login page existence
    expect(wrapper.findComponent(Login).exists()).toBeTruthy();

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

    await loginButton.trigger('submit');

    await Vue.nextTick();
    //bottom line shows an error because it calls on the actual method, which only exists in the file.
    //but for the purpose of this test, that does not matter.
    expect(methodInvoked).toBeCalled();
  });
});

describe('Login.vue', () => {
  let wrapper: Wrapper<any>;
  const localVue = createLocalVue();
  let vuetify: Vuetify;

  beforeEach(() => {
    vuetify = new Vuetify();
    wrapper = mount(Login, {
      localVue,
      vuetify
    });
  });

  it("Testing out the inputs for the email and password, such that the user can only press the login button " +
    "after inputting valid formats for both fields", async () => {
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

  it("Testing for invalid email format, with less than two characters after each '.'", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    await emailInput.setValue('someemail@gmail.c');
    let passwordInput = wrapper.find('input[type="password"]');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format,with no '@'", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    await emailInput.setValue('someemail.com');
    let passwordInput = wrapper.find('input[type="password"]');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, with no characters before '@'", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    await emailInput.setValue('@gmail.com');
    let passwordInput = wrapper.find('input[type="password"]');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, with no '.'", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    //wrong email format, with no "."
    let emailInput = wrapper.find('input[type="email"]');
    await emailInput.setValue('fsefsgr@gmailcom');
    let passwordInput = wrapper.find('input[type="password"]');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid email format, empty email field", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    let passwordInput = wrapper.find('input[type="password"]');
    await emailInput.setValue('');
    await passwordInput.setValue('hello123');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  // !!!NOTICE!!!
  // this test lags the whole application, so is commented out for the moment
  // it("Testing for invalid email format, over character limit", async () => {
  //   const loginButton = wrapper.find(".v-btn");
  //   expect(loginButton.props().disabled).toBeTruthy();
  //
  //   let emailInput = wrapper.find('input[type="email"]');
  //   let passwordInput = wrapper.find('input[type="password"]');
  //   await emailInput.setValue(
  //     'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
  //   );
  //   await passwordInput.setValue('hello123');
  //   await Vue.nextTick(() => {
  //     expect(loginButton.props().disabled).toBeTruthy();
  //   });
  // });

  it("Testing for invalid password format, with no numbers", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    let passwordInput = wrapper.find('input[type="password"]');
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('hello');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, with no alphabets", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    let passwordInput = wrapper.find('input[type="password"]');
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('123455678');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, with less than 7 characters", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    let passwordInput = wrapper.find('input[type="password"]');
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('abcd1');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  it("Testing for invalid password format, empty password field", async () => {
    const loginButton = wrapper.find(".v-btn");
    expect(loginButton.props().disabled).toBeTruthy();

    let emailInput = wrapper.find('input[type="email"]');
    let passwordInput = wrapper.find('input[type="password"]');
    await emailInput.setValue('someemail@gmail.com');
    await passwordInput.setValue('');
    await Vue.nextTick(() => {
      expect(loginButton.props().disabled).toBeTruthy();
    });
  });

  // !!!NOTICE!!!
  // this test lags the whole application, so is commented out for the moment
  // it("Testing for invalid password format, over character limit", async () => {
  //   const loginButton = wrapper.find(".v-btn");
  //   expect(loginButton.props().disabled).toBeTruthy();
  //
  //   let emailInput = wrapper.find('input[type="email"]');
  //   let passwordInput = wrapper.find('input[type="password"]');
  //
  //   await emailInput.setValue('someemail@gmail.com');
  //   await passwordInput.setValue(
  //     'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'
  //   );
  //   await Vue.nextTick(() => {
  //     expect(loginButton.props().disabled).toBeTruthy();
  //   });
  // });
});