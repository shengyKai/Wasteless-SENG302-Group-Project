import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';
import ImageSelector from "@/components/utils/ImageSelector.vue";

Vue.use(Vuetify);

const localVue = createLocalVue();

const openFileDialogMock = jest.spyOn((ImageSelector as any).methods, 'openFileDialog');

describe('ProductImageUploader.vue', () => {
  // Container for the wrapper around ImageSelector
  let appWrapper: Wrapper<any>;

  // Container for the ImageSelector under test
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


  beforeEach(() => {
    const vuetify = new Vuetify();

    // Creating wrapper around ProductImageUploader with data-app to appease vuetify
    const App = localVue.component('App', {
      components: { ImageSelector },
      template: `
      <div data-app>
        <ImageSelector v-model="file"/>
      </div>`,
    });

    // Put the ProductImageUploader component inside a div in the global document,
    // this seems to make vuetify work correctly, but necessitates calling appWrapper.destroy
    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      data() {
        return {
          file: undefined
        };
      }
    });

    wrapper = appWrapper.getComponent(ImageSelector);
  });

  /**
   * Executes after every test case.
   *
   * This function makes sure that the ImageSelector component is removed from the global document
   */
  afterEach(() => {
    appWrapper.destroy();
  });

  /**
   * Finds the select file button in the ImageSelector component.
   *
   * @returns A Wrapper around the select button
   */
  function findSelectButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.text().includes('Select'));
    expect(filtered.length).toBe(1);
    return filtered.at(0);
  }

  /**
   * Finds the discard selected image button in the ImageSelector component if it exists
   *
   * @returns A wrapper around the discard image button if it exists, undefined otherwise
   */
  function findDiscardImageButton() {
    const buttons = wrapper.findAllComponents({ name: 'v-btn' });
    const filtered = buttons.filter(button => button.findComponent({name: 'v-icon' }).exists());
    expect(filtered.length).toBeLessThanOrEqual(1);
    return filtered.length === 1 ? filtered.at(0) : undefined;
  }

  it('Clicking the select button should trigger the "openFileDialog" method', async () => {
    findSelectButton().trigger('click');
    expect(openFileDialogMock).toHaveBeenCalled();
  });

  it('Dropping a file should set the file attribute', async () => {
    wrapper.vm.isDragging = true;
    const testFile = new File([], 'test_file');
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        files: [
          testFile
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDrop(event);
    expect(appWrapper.vm.file).toBe(testFile);
    expect(wrapper.vm.isDragging).toBeFalsy();
    expect(event.preventDefault).toBeCalled();
  });

  it('Dragging state should be enabled when the "onDragOver" event is triggered with a valid file', async () => {
    expect(wrapper.vm.isDragging).toBeFalsy();
    const matchFn = jest.fn().mockReturnValue(true);
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        items: [
          {
            kind: 'file',
            type: {
              match: matchFn,
            }
          }
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDragOver(event);
    expect(matchFn).toBeCalledWith('^image/');
    expect(wrapper.vm.isDragging).toBeTruthy();
    expect(event.preventDefault).toBeCalled();
  });

  it('Dragging state should not be enabled when the "onDragOver" event is triggered with a invalid file', async () => {
    wrapper.vm.isDragging = false;
    const matchFn = jest.fn().mockReturnValue(false); // Non-matching file
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        items: [
          {
            kind: 'file',
            type: {
              match: matchFn,
            }
          }
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDragOver(event);
    expect(matchFn).toBeCalledWith('^image/');
    expect(wrapper.vm.isDragging).toBeFalsy();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('Dragging state should not be enabled when the "onDragOver" event is triggered with a invalid drag type', async () => {
    wrapper.vm.isDragging = false;
    const matchFn = jest.fn().mockReturnValue(true); // Non-matching file
    const event = { // Mock version of the dragOver event.
      dataTransfer: {
        items: [
          {
            kind: 'not-file',
            type: {
              match: matchFn,
            }
          }
        ]
      },
      preventDefault: jest.fn(),
    };
    wrapper.vm.onDragOver(event);
    expect(wrapper.vm.isDragging).toBeFalsy();
    expect(event.preventDefault).not.toHaveBeenCalled();
  });

  it('When no image is selected then the discard image button should not exist', () => {
    expect(findDiscardImageButton()).toBeUndefined();
  });

  it('When an image is selected the discard image button should exist and clicking it should discard the image', async () => {
    const testFile = new File([], 'test_file');
    wrapper.vm.file = testFile;
    await Vue.nextTick();
    const discardImage = findDiscardImageButton();
    expect(discardImage).not.toBeUndefined();
    discardImage!.trigger('click');
    await Vue.nextTick();
    expect(wrapper.vm.file).toBeUndefined();
    expect(findDiscardImageButton()).toBeUndefined();
  });


});