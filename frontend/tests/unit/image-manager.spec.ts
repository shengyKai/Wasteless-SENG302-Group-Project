import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ImageManager from "@/components/utils/ImageManager.vue";
import ImageUploader from "@/components/utils/ImageUploader.vue";
import { castMock } from './utils';
import { uploadImage } from "@/api/images";

jest.mock('@/api/images', () => ({
  uploadImage: jest.fn(),
}));

const uploadImageMock = castMock(uploadImage);

Vue.use(Vuetify);

const localVue = createLocalVue();

describe('ImageManager.vue', () => {
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

    wrapper = mount(ImageManager, {
      localVue,
      vuetify,
      stubs: {
        ImageUploader
      },
      propsData: {
        images: [  
          {
            id: 1,
            filename: "some test file",
            thumbnailFilename: "some thumbnail"
          }
        ]
      }
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("Only shows the single upload icon if the images prop array length is lesser than 1", async () => {
    await wrapper.setProps({
      images: []
    });
    expect(wrapper.findComponent({ref: "uploadWithNoImages"}).exists()).toBe(true);
    expect(wrapper.findComponent({ref: "upload"}).exists()).toBe(false);
    expect(wrapper.findComponent({ref: "trashCan"}).exists()).toBe(false);
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBe(false);
  });

  it("Shows the upload, trash can and make primary icon if the images prop array length is more than or equal to 1", async () => {
    expect(wrapper.findComponent({ref: "uploadWithNoImages"}).exists()).toBe(false);
    expect(wrapper.findComponent({ref: "upload"}).exists()).toBe(true);
    expect(wrapper.findComponent({ref: "trashCan"}).exists()).toBe(true);
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBe(true);
  });
});