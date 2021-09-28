import Vue from 'vue';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';
import Vuex from 'vuex';
import Avatar from "@/components/utils/Avatar.vue";
import { castMock } from './utils';
import { imageSrcFromFilename } from '@/utils';

Vue.use(Vuetify);

jest.mock('@/utils', () => ({
  imageSrcFromFilename: jest.fn(),
}));

const imageScrFromFilenameMock = castMock(imageSrcFromFilename);

describe("Avatar.vue", () => {

  let wrapper: Wrapper<any>;

  beforeEach(async () => {
    const localVue = createLocalVue();

    localVue.use(Vuex);

    wrapper = mount(Avatar, {
      localVue,
      vuetify: new Vuetify(),
      propsData: {
        user: {
          firstName: "Jeff",
          lastName: "Bezos",
          images: [
            {
              id: 3,
              filename: "file.jpg",
              thumbnailFilename: "thumbnail.jpg"
            }
          ]
        },
        business: undefined
      }
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it('When business is provided, avatar is set to business\'s primary image', async () => {
    imageScrFromFilenameMock.mockReturnValueOnce("http://localhost:9500/businessThumbnail.jpg");
    await wrapper.setProps({
      user: undefined,
      business: {
        name: "Business",
        images: [
          {
            id: 3,
            filename: "businessFile.jpg",
            thumbnailFilename: "businessThumbnail.jpg"
          }
        ]
      },
    });
    expect(imageScrFromFilenameMock).toBeCalledWith("businessThumbnail.jpg");
    expect(wrapper.vm.image).toBe("http://localhost:9500/businessThumbnail.jpg");
  });

  it('When business is provided, initals are derived from business name', async () => {
    await wrapper.setProps({
      user: undefined,
      business: {
        name: "Business",
        images: [
          {
            id: 3,
            filename: "businessFile.jpg",
            thumbnailFilename: "businessThumbnail.jpg"
          }
        ]
      },
    });
    expect(wrapper.vm.initials).toBe("B");
  });

  it('When user is provided, avatar is set to user\'s primary image', async () => {
    imageScrFromFilenameMock.mockReturnValueOnce("http://localhost:9500/userThumbnail.jpg");
    await wrapper.setProps({
      user: {
        firstName: "Jeff",
        lastName: "Bezos",
        images: [
          {
            id: 3,
            filename: "userFile.jpg",
            thumbnailFilename: "userThumbnail.jpg"}
        ]
      },
      business: undefined,
    });
    expect(imageScrFromFilenameMock).toBeCalledWith("userThumbnail.jpg");
    expect(wrapper.vm.image).toBe("http://localhost:9500/userThumbnail.jpg");
  });

  it("When user is provided, initials are derived from user\'s first and last name", async () => {
    await wrapper.setProps({
      user: {
        firstName: "Jeff",
        lastName: "Bezos",
        images: [
          {
            id: 3,
            filename: "userFile.jpg",
            thumbnailFilename: "userThumbnail.jpg"}
        ]
      },
      business: undefined,
    });
    expect(wrapper.vm.initials).toBe("JB");
  });
});
