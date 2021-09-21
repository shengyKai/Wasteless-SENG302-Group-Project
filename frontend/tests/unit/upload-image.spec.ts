import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import ImageUploader from "@/components/utils/ImageUploader.vue";
import { castMock, findButtonWithText } from './utils';
import { uploadImage } from "@/api/images";

jest.mock('@/api/images', () => ({
  uploadImage: jest.fn(),
}));

const uploadImageMock = castMock(uploadImage);

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('ImageUploader.vue', () => {
  // Container for the wrapper around ImageUploader
  let appWrapper: Wrapper<any>;

  // Container for the ImageUploader under test
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
   * Sets up the test ImageUploader instance
   *
   * Because the element we're testing has a v-dialog we need to take some extra sets to make it
   * work.
   */
  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around ImageUploader with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { ImageUploader },
      template: `
      <div data-app>
        <ImageUploader v-model="file"/>
      </div>`,
    });

    // Put the ImageUploader component inside a div in the global document,
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

    wrapper = appWrapper.getComponent(ImageUploader);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the ImageUploader component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Finds the close button in the ImageUploader form
   *
   * @returns A Wrapper around the close button
   */
  const findCloseButton = () => findButtonWithText(wrapper, 'Close');

  /**
   * Finds the create button in the ImageUploader form
   *
   * @returns A Wrapper around the create button
   */
  const findUploadButton = () => findButtonWithText(wrapper, 'Upload');

  it('When the close button is pressed then the dialog should be closed', async () => {
    await findCloseButton().trigger('click'); // Click close button
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  it('When the upload button is pressed then an image should be uploaded', async () => {
    const testFile = new File([], 'test_file');
    await wrapper.setData({
      file: testFile
    });
    await findUploadButton().trigger('click'); // Click upload button
    expect(uploadImageMock).toBeCalledTimes(1);
  });

  it('When the upload button is pressed then the dialog should be closed', async () => {
    const testFile = new File([], 'test_file');
    await wrapper.setData({
      file: testFile
    });
    await findUploadButton().trigger('click'); // Click upload button
    expect(wrapper.emitted().closeDialog).toBeTruthy();
  });

  it('When there is no image selected then the upload button should be disabled', async () => {
    expect(findUploadButton().props().disabled).toBeTruthy();
  });

  it('When there is an image selected then the upload button should be enabled', async () => {
    const testFile = new File([], 'test_file');
    await wrapper.setData({
      file: testFile
    });
    expect(findUploadButton().props().disabled).toBeFalsy();
  });

});
