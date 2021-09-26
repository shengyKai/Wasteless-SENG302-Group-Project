import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ImageManager from "@/components/image/ImageManager.vue";
import ImageUploader from "@/components/image/ImageUploader.vue";

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
        value: [
          {
            id: 1,
            filename: "some test file",
            thumbnailFilename: "some thumbnail"
          }
        ]
      },
      stubs: ['ImageUploader']
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("Only shows the single upload icon if the images prop array length is lesser than 1", async () => {
    await wrapper.setData({
      outputImages: []
    });
    expect(wrapper.findComponent({ref: "uploadWithNoImages"}).exists()).toBeTruthy();
    expect(wrapper.findComponent({ref: "upload"}).exists()).toBeFalsy();
    expect(wrapper.findComponent({ref: "trashCan"}).exists()).toBeFalsy();
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBeFalsy();
  });

  it("Shows the upload and trash can icon if the images prop array length is more than or equal to 1", () => {
    expect(wrapper.findComponent({ref: "uploadWithNoImages"}).exists()).toBeFalsy();
    expect(wrapper.findComponent({ref: "upload"}).exists()).toBeTruthy();
    expect(wrapper.findComponent({ref: "trashCan"}).exists()).toBeTruthy();
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBeFalsy();
  });

  it("Shows the make image primary icon if there are at least two images", async () => {
    const images = [
      {
        id: 1,
        filename: "some test file",
        thumbnailFilename: "some thumbnail"
      },
      {
        id: 2,
        filename: "another test file",
        thumbnailFilename: "some thumbnail"
      }
    ];
    await wrapper.setData({outputImages: images, model:1});
    expect(wrapper.findComponent({ref: "makePrimary"}).exists()).toBeTruthy();
  });

  it('If the uploadWithNoImages icon is clicked, the ImageUploader will be shown', async() => {
    await wrapper.setData({
      outputImages: []
    });
    await wrapper.findComponent({ref: "uploadWithNoImages"}).trigger("click");
    expect(wrapper.findComponent(ImageUploader).exists()).toBeTruthy();
  });

  it('If the upload icon is clicked, the ImageUploader will be shown', async() => {
    await wrapper.findComponent({ref: "upload"}).trigger("click");
    expect(wrapper.findComponent(ImageUploader).exists()).toBeTruthy();
  });

  it("If the imageUploader uploads an image, the output images will be updated", async () => {
    const anotherImage =  {
      id: 2,
      filename: "some test file 2",
      thumbnailFilename: "some thumbnail 2"
    };
    await wrapper.setData({showImageUploader:true, uploadedImage: anotherImage});
    const imageUploader = wrapper.findComponent(ImageUploader);
    expect(wrapper.vm.outputImages.length).toEqual(1);
    imageUploader.vm.$emit("upload");
    await Vue.nextTick();
    expect(wrapper.vm.outputImages.length).toEqual(2);
    expect(wrapper.emitted('input')).toBeTruthy();
  });

  it("If the delete method is called, the image is removed", async () => {
    expect(wrapper.vm.outputImages.length).toEqual(1);
    await wrapper.vm.deleteImage(wrapper.vm.outputImages[0]);
    await Vue.nextTick();
    expect(wrapper.vm.outputImages.length).toEqual(0);
    expect(wrapper.emitted('input')).toBeTruthy();
  });

  it("If the delete method is called with an image which doesn't exist, no image is removed", async () => {
    expect(wrapper.vm.outputImages.length).toEqual(1);
    wrapper.vm.deleteImage({
      id: 2,
      filename: "wrong image test file",
      thumbnailFilename: "some thumbnail"
    });
    await Vue.nextTick();
    expect(wrapper.vm.outputImages.length).toEqual(1);
  });

  it("If the makeImagePrimary is called, the image becomes the front of the output list", async ()=> {
    const images = [
      {
        id: 1,
        filename: "some test file",
        thumbnailFilename: "some thumbnail"
      },
      {
        id: 2,
        filename: "another test file",
        thumbnailFilename: "some thumbnail"
      }
    ];
    await wrapper.setData({outputImages:images});
    const image1 = wrapper.vm.outputImages[0];
    const image2 = wrapper.vm.outputImages[1];
    wrapper.vm.makeImagePrimary(image2);
    expect(wrapper.vm.outputImages[0]).toBe(image2);
    expect(wrapper.vm.outputImages[1]).toBe(image1);
    expect(wrapper.emitted('input')).toBeTruthy();
  });
});