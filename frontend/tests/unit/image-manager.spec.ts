import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ImageManager from "@/components/utils/ImageManager.vue";
import { castMock } from './utils';
import { uploadImage } from "@/api/images";

jest.mock('@/api/images', () => ({
  uploadImage: jest.fn(),
}));

const uploadImageMock = castMock(uploadImage);

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('ImageManager.vue', () => {
  let appWrapper: Wrapper<any>;
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

    const App = localVue.component('App', {
      components: { ImageManager },
      template: `
      <div data-app>
        <ImageManager :images="images"/>
      </div>`,
    });

    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      localVue,
      vuetify,
      attachTo: elem,
      data() {
        return {
          images: []
        };
      }
    });

    wrapper = appWrapper.getComponent(ImageManager);
  });

  afterEach(() => {
    appWrapper.destroy();
  });

  it("Only shows the single upload button if the images prop array length is lesser than 1", async () => {
    const uploadWithNoImagesIcons = wrapper.findAllComponents({ ref: "uploadWithNoImages" });
    const uploadIcons = wrapper.findAllComponents({ ref: "upload" });
    const trashCanIcons = wrapper.findAllComponents({ ref: "trashCan" })
    const makePrimaryIcons = wrapper.findAllComponents({ ref: "makePrimary" })

    expect(uploadWithNoImagesIcons.length).toEqual(1);
    expect(uploadIcons.length).toEqual(0);
    expect(trashCanIcons.length).toEqual(0);
    expect(makePrimaryIcons.length).toEqual(0);
  });
});