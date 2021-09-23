import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ImageManager from "@/components/utils/ImageManager.vue";
import ImageUploader from "@/components/utils/ImageUploader.vue";

Vue.use(Vuetify);

const localVue = createLocalVue();


describe('ImageManager.vue', () => {
  let wrapper: Wrapper<any>;

  beforeEach(() => {
    const vuetify = new Vuetify();

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append(app);

    wrapper = mount(ImageManager, {
      localVue,
      vuetify,
      components: {
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
    expect(wrapper.findComponent({ref: "uploadWithNoImages"}).exists()).toBeTruthy();
    expect(wrapper.findComponent({ref: "upload"}).exists()).toBeFalsy();
    expect(wrapper.findComponent({ref: "trashCan"}).exists()).toBeFalsy();
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBeFalsy();
  });

  it("Shows the upload, trash can and make primary icon if the images prop array length is more than or equal to 1", () => {
    expect(wrapper.findComponent({ref: "uploadWithNoImages"}).exists()).toBeFalsy();
    expect(wrapper.findComponent({ref: "upload"}).exists()).toBeTruthy();
    expect(wrapper.findComponent({ref: "trashCan"}).exists()).toBeTruthy();
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBeTruthy();
  });

  it('If the uploadWithNoImages icon is clicked, the ImageUploader will be shown', async() => {
    await wrapper.setProps({
      images: []
    });
    await wrapper.findComponent({ref: "uploadWithNoImages"}).trigger("click");
    expect(wrapper.findComponent(ImageUploader).exists()).toBeTruthy();
  });

  it('If the upload icon is clicked, the ImageUploader will be shown', async() => {
    await wrapper.findComponent({ref: "upload"}).trigger("click");
    expect(wrapper.findComponent(ImageUploader).exists()).toBeTruthy();
  });

  it("If there is an uploadedImage, toBeSubmittedImages array will be updated while the images array still stays the same ", async () => {
    const anotherImage =  {
      id: 2,
      filename: "some test file 2",
      thumbnailFilename: "some thumbnail 2"
    };
    await wrapper.setData({
      uploadedImage: anotherImage
    });
    expect(wrapper.vm.toBeSubmittedImages.length).toEqual(1);
    expect(wrapper.vm.images.length).toEqual(1);
    await wrapper.vm.upload();
    expect(wrapper.vm.toBeSubmittedImages.length).toEqual(2);
    expect(wrapper.vm.images.length).toEqual(1);
  });
});