import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import BusinessImageUploader from "@/components/utils/BusinessImageUploader.vue";
import { castMock, flushQueue } from './utils';
import * as api from '@/api/internal';

jest.mock('@/api/internal', () => ({
  uploadBusinessImage: jest.fn(),
}));

const uploadBusinessImage = castMock(api.uploadBusinessImage);

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('BusinessImageUploader.vue', () => {
  // Container for the wrapper around BusinessImageUploader
  let appWrapper: Wrapper<any>;

  // Container for the BusinessImageUploader under test
  let wrapper: Wrapper<any>;


  /**
   * Executes before all the tests.
   *
   * The jsdom environment doesn't declare URL.createObjectURL function, hence we need to implement it
   * ourselves to make the uploader not crash.
   */
  beforeAll(() => {
    globalThis.URL = {
      createObjectURL(object: any) {
        return 'obj_url:' + object;
      }
    } as any;
  });

  /**
   * Sets up the test BusinessImageUploader instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around BusinessImageUploader with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { BusinessImageUploader },
      template: `
      <div data-app>
        <BusinessImageUploader v-model="file"/>
      </div>`,
    });

    // Put the BusinessImageUploader component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      data() {
        return {
          file: undefined,
        };
      }
    });

    wrapper = appWrapper.getComponent(BusinessImageUploader);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the BusinessImageUploader component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Finds the close button in the BusinessImageUploader form
   *
   * @returns A Wrapper around the close button
   */
  function findCloseButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Close'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the create button in the BusinessImageUploader form
   *
   * @returns A Wrapper around the create button
   */
  function findUploadButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Upload'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  it('When the close button is pressed then the dialog should be closed', async () => {
    await findCloseButton().trigger('click'); // Click close button
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  it('When the upload button is pressed then an image should be uploaded', async () => {
    const testFile = new File([], 'test_file');
    appWrapper.vm.file = testFile;
    await Vue.nextTick();
    await findUploadButton().trigger('click'); // Click upload button
    console.log(wrapper.emitted());
    expect(wrapper.emitted().uploadImage).toBeTruthy();
  });

  it('When there is no image selected then the upload button should be disabled', async () => {
    await Vue.nextTick();
    expect(findUploadButton().props().disabled).toBeTruthy();
  });

  it('When there is an image selected then the upload button should be enabled', async () => {
    const testFile = new File([], 'test_file');
    appWrapper.vm.file = testFile;
    await Vue.nextTick();
    expect(findUploadButton().props().disabled).toBeFalsy();
  });

});
