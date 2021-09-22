import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import {createLocalVue, mount, Wrapper} from '@vue/test-utils';

import ModifyBusiness from '@/components/BusinessProfile/ModifyBusiness.vue';
import {castMock, findButtonWithText} from "./utils";
import {Location} from '@/api/internal';
import {getStore, resetStoreForTesting} from '@/store';
import {getUser as getUser1, User} from "@/api/user";
import {modifyBusiness as modifyBusiness1, uploadBusinessImage as uploadBusinessImage1, Business} from "@/api/business";

jest.mock('@/api/user', () => ({
  getUser: jest.fn(),
}));
jest.mock('@/api/business', () => ({
  modifyBusiness: jest.fn(),
  uploadBusinessImage: jest.fn()
}));


const getUser = castMock(getUser1);
const modifyBusiness = castMock(modifyBusiness1);
const uploadBusinessImage = castMock(uploadBusinessImage1);
Vue.use(Vuetify);
const localVue = createLocalVue();

/**
 * Creates a test location
 *
 * @returns The generated location
 */
function createTestLocation() {
  let location: Location = {
    streetNumber: '10',
    streetName: 'Downing Street',
    city: 'London',
    region: 'England',
    postcode: '1234',
    district: 'Westminster',
    country: 'United Kingdom'
  };
  return location;
}

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @returns The generated user
 */
function createTestUser(userId: number) {
  let user: User = {
    id: userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: createTestLocation()
  };
  return user;
}

/**
 * Creates a test business with the given business id
 *
 * @param businessId The business id to use
 * @param primaryAdminId the id of the user who will be the primary administrator
 * @param admins a list of users to be the administrators of the businesses
 */
function createTestBusiness(businessId: number, primaryAdminId: number, admins: User[]) {
  let business: Business = {
    id: businessId,
    primaryAdministratorId: primaryAdminId,
    administrators: admins,
    name: 'test_businessname' + businessId,
    description: 'test_description' + businessId,
    address: createTestLocation(),
    businessType: 'Charitable organisation',
    created: '2/6/2006'
  };
  return business;
}

describe('modifyBusiness.vue', () => {
  // Container for the wrapper that goes around Modify Business
  let appWrapper: Wrapper<any>;

  // Container for the ModifyBusiness used in these tests
  let wrapper: Wrapper<any>;

  /**
   * Executes before all the tests.
   *
   * The jsdom environment doesn't declare the fetch function, hence we need to implement it
   * ourselves to make LocationAutocomplete not crash.
   */
  beforeAll(() => {
    globalThis.fetch = async () => {
      return {
        json() {
          return {
            features: [],
          };
        }
      } as any;
    };
  });

  let testUser: User;
  let testAdmins: User[] = [];

  /**
   * Sets up the test ModifyBusiness instance
   */
  beforeEach(() => {
    localVue.use(Vuex);
    testUser = createTestUser(1);
    testAdmins.push(testUser);
    testAdmins.push(createTestUser(69));
    testAdmins.push(createTestUser(3));
    const business = createTestBusiness(44, 1, testAdmins);
    resetStoreForTesting();
    let store = getStore();
    store.state.user = testUser;
    getUser.mockResolvedValueOnce(testUser);
    const vuetify = new Vuetify();
    const App = localVue.component('App', {
      components: { ModifyBusiness },
      template: '<div data-app><ModifyBusiness :business="thingy"/></div>',
    });

    const elem = document.createElement('div');
    document.body.appendChild(elem);

    appWrapper = mount(App, {
      stubs: ['router-link', 'router-view'],
      mocks: {
        $router: {
          go: () => {return;},
        }
      },
      localVue,
      vuetify,
      attachTo: elem,
      store: store,
      data() {
        return {
          thingy: business
        };
      }
    });

    wrapper = appWrapper.getComponent(ModifyBusiness);
  });

  afterEach(() => {
    appWrapper.destroy();
    testAdmins = [];
  });

  /**
   * Finds the associated button in the Modify Business form
   *
   * @returns A wrapper around the update button
   */
  function findButton(component:string) {
    return findButtonWithText(wrapper, component);
  }


  /**
   * Adds all the fields that are required for the create business form to be valid
   *
   * These are:
   * - Business name
   * - Business type
   * - Street address line 1
   * - City
   * - Region
   * - Country
   * - Postcode
   */
  async function populateRequiredFields() {
    await wrapper.setData({
      businessName: 'Business Name',
      businessType: 'Retail Trade',
      streetAddress: '1 Elizabeth Street',
      city: 'City',
      region: 'Region',
      country: 'Country',
      postcode: '1234',
    });
  }

  it('Valid if all fields are provided', async () => {
    await populateRequiredFields();
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new name of the business is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessName: 'Fabian Fabrication'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new description of the business is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'Fabian Fabrication focuses on the highest quality fabrication that works for you'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new business type of the business is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessType: 'Retail Trade'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new street address is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '69 Happy Street'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new district is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      district: 'Riccarton'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new city is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: 'Christchurch'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new region is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: 'Canterbury'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new country is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: 'New Zealand'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the new postcode is provided', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: '8041'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Invalid if the new business name is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessName: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the new business type is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessType: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Valid if the new description is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Invalid if the new street address is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Valid if the new district is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      district: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Invalid if the new city is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the new region is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the new country is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the new postcode is empty', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: ''
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the new business name is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      businessName: 'e'.repeat(101)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the new description is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      description: 'e'.repeat(201)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the street address contains a character', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '69 Eliz@beth Street'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the street address only contains a number', async() => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '69'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the street address only contains a word', async() => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: 'Elizabeth Street'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the street address is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '69 Street ' + 'e'.repeat(100)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Valid if the street have has 1st in it', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '1 1st Avenue'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the street have has 2nd in it', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '69 2nd Street'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Valid if the street have has 3rd in it', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      streetAddress: '419 3rd Crescent'
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeTruthy();
  });

  it('Invalid if the district is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      district: 'e'.repeat(101)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the city is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      city: 'e'.repeat(101)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the region is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      region: 'e'.repeat(101)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if the country is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      country: 'e'.repeat(101)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it('Invalid if postcode is too long', async () => {
    await populateRequiredFields();
    await wrapper.setData({
      postcode: 'e'.repeat(101)
    });
    await Vue.nextTick();
    expect(wrapper.vm.valid).toBeFalsy();
  });

  it("If all fields are populated with the right restrictions and the submit button is clicked, the modifyBusiness endpoint is called", async () => {
    await populateRequiredFields();
    const submitButton = findButton("Submit");
    await submitButton.trigger('click');
    expect(modifyBusiness).toHaveBeenCalled();
  });

  describe('changing primary administrator', () => {
    it('Primary admin is changed and alert message is shown when non-primary admin is selected', async() => {
      const currentPrimaryAdmin = testAdmins[0];
      const newPrimaryAdmin = testAdmins[1];
      expect(wrapper.vm.adminIsPrimary(newPrimaryAdmin)).toBeFalsy();
      expect(wrapper.vm.adminIsPrimary(currentPrimaryAdmin)).toBeTruthy();
      wrapper.vm.changePrimaryOwner(newPrimaryAdmin);
      expect(wrapper.vm.primaryAdminAlertMsg).toEqual(`Primary admin will be changed to ${newPrimaryAdmin.firstName} ${newPrimaryAdmin.lastName}`);
    });

    it('Primary admin stays the same and alert message is not shown when primary admin is selected', async() => {
      const primaryAdmin = testAdmins[0];
      expect(wrapper.vm.adminIsPrimary(primaryAdmin)).toBeTruthy();
      wrapper.vm.changePrimaryOwner(primaryAdmin);
      expect(wrapper.vm.adminIsPrimary(primaryAdmin)).toBeTruthy();
      expect(wrapper.vm.primaryAdminAlertMsg).toEqual('');
    });
  });

  describe('Uploading images', () => {
    beforeEach(() => {
      uploadBusinessImage.mock.calls = [];
      uploadBusinessImage.mockResolvedValue(undefined);
    });

    it('When image file added, it is appended to the list of image files', () => {
      expect(wrapper.vm.allImageFiles.length).toBe(0);
      const testFile = new File([], 'test_file');
      wrapper.vm.imageFile = testFile;

      wrapper.vm.addImage();

      expect(wrapper.vm.allImageFiles.length).toBe(1);
      expect(wrapper.vm.allImageFiles[0]).toBe(testFile);
    });

    it('When form is submitted and no images are added, no API call to upload images is made', async () => {
      expect(wrapper.vm.allImageFiles.length).toBe(0);
      await wrapper.vm.proceedWithModifyBusiness();
      expect(uploadBusinessImage.mock.calls.length).toBe(0);
    });

    it('When form is submitted and one image is added, one API call to upload images is made', async () => {
      const testFile1 = new File([], 'test_file_1');
      wrapper.vm.allImageFiles = [testFile1];
      expect(wrapper.vm.allImageFiles.length).toBe(1);
      await wrapper.vm.proceedWithModifyBusiness();
      expect(uploadBusinessImage.mock.calls.length).toBe(1);
      expect(uploadBusinessImage.mock.calls[0][0]).toBe(44);
      expect(uploadBusinessImage.mock.calls[0][1]).toBe(testFile1);
    });

    it('When form is submitted and multiple images are added, one API call to upload images is made for each image', async () => {
      wrapper.vm.allImageFiles = [new File([], 'test_file_1'), new File([], 'test_file_2'), new File([], 'test_file_3')];
      expect(wrapper.vm.allImageFiles.length).toBe(3);
      await wrapper.vm.proceedWithModifyBusiness();
      expect(uploadBusinessImage.mock.calls.length).toBe(3);
    });

    it('When upload image request is successful, no error message is displayed', async () => {
      expect(wrapper.vm.errorMessage).toBe(undefined);
      wrapper.vm.allImageFiles = [new File([], 'test_file_1')];
      await wrapper.vm.proceedWithModifyBusiness();
      expect(wrapper.vm.errorMessage).toBe(undefined);
    });

    it('When upload image request is unsuccessful, received error message is displayed', async () => {
      expect(wrapper.vm.errorMessage).toBe(undefined);
      uploadBusinessImage.mockResolvedValueOnce("This is an error");
      wrapper.vm.allImageFiles = [new File([], 'test_file_1')];
      await wrapper.vm.proceedWithModifyBusiness();
      expect(wrapper.vm.errorMessage).toBe("This is an error");
    });

  });
});
