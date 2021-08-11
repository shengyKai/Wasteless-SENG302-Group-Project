import Vue from 'vue';
import Vuex from 'vuex';
import Vuetify from 'vuetify';
import { createLocalVue, Wrapper, mount } from '@vue/test-utils';

import ModifyBusiness from '@/components/BusinessProfile/index.vue';
import {castMock} from "./utils";
import * as api from '@/api/internal';
import {User, Location, Business} from "@/api/internal";

jest.mock('@/api/internal', () => ({
  modifyBusiness: jest.fn(),
}));

const modifyBusiness = castMock(api.modifyBusiness);
Vue.use(Vuetify);
const localVue = createLocalVue();

/**
 * Creates a test location
 * 
 * @returns The generated location
 */
function createTestLocation() {
  let location: Location = {
    streetNumber: 'test_street_number',
    streetName: 'test_streetAddress',
    city: 'test_city',
    region: 'test_region',
    postcode: 'test_postcode',
    district: 'test_district',
    country: 'test_country'
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
