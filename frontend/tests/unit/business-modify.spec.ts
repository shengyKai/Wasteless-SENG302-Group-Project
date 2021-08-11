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


//function createTestBusiness(businessId: number, adminId: number, admins: User[]) {
//  let business: Business = {
//    id: businessId,
//    primaryAdministratorId: adminId,
//    administrators: admins,
//    name: 'test_businessname' + b
//  }
//}
