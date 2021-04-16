import Vue from 'vue';
import VueRouter from 'vue-router';
import Vuetify from 'vuetify';
import Vuex, { Store } from 'vuex';
import { shallowMount, createLocalVue, Wrapper } from '@vue/test-utils';
import App from '@/App.vue';
import { createOptions, StoreData } from '@/store';
import { VAlert } from 'vuetify/lib';


Vue.use(Vuetify);

const localVue = createLocalVue();

localVue.use(VueRouter);
const router = new VueRouter();

describe('GlobalError', () => {
  // Container for the App under test
  let wrapper: Wrapper<any>;
  let store: Store<StoreData>;

  /**
   * Sets up a test App instance.
   */
  beforeEach(() => {
    localVue.use(Vuex);
    let options = createOptions();
    options.state = {
      user: null,
      activeRole: null,
      globalError: null,
      createBusinessDialogShown: false,
    };
    store = new Vuex.Store(options);

    wrapper = shallowMount(App, {
      localVue,
      store,
      router,
      // stubs: {
      //   'v-alert': VAlert,
      // },
    });
  });

  /**
   * Finds the current error message component if it exists.
   *
   * @returns Error message component wrapper or if doesn't exist an error wrapper
   */
  function findErrorBox() {
    // Since we're doing a shallow mount and so long as we don't add a new v-alert component to
    // App.vue, then this selector should be sufficient.
    return wrapper.findComponent({name: 'v-alert'});
  }

  it('If no error then no error component is displayed', () => {
    expect(findErrorBox().exists()).toBeFalsy();
  });

  it('If there is an error then there should be an error component with the right message', async () => {
    store.commit('setError', 'test_error_message');

    await Vue.nextTick();

    let errorBox = findErrorBox();
    expect(errorBox.exists()).toBeTruthy();
    expect(errorBox.text()).toBe('test_error_message');
  });

  it('If the error is cleared then there should be no error component', async () => {
    store.commit('setError', 'test_error_message');
    await Vue.nextTick();
    store.commit('clearError');
    await Vue.nextTick();

    expect(findErrorBox().exists()).toBeFalsy();
  });

  it('If the router changes pages then the error component should disappear', async () => {
    store.commit('setError', 'test_error_message');
    await Vue.nextTick();
    router.push('/unimportant');
    await Vue.nextTick();

    expect(findErrorBox().exists()).toBeFalsy();
  });

  // it('If there is an error then we should be able to dismiss it', async () => {
  //   store.commit('setError', 'test_error_message');

  //   await Vue.nextTick();

  //   let errorBox = findErrorBox();
  //   expect(errorBox.exists()).toBeTruthy();

  //   console.log(errorBox.html());

  //   let thingy = errorBox.findComponent({name: 'v-btn' });
  //   console.log(thingy);
  // });
});
