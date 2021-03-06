import {User, UserRole} from "@/api/user";
import {Business} from "@/api/business";
import { Wrapper } from "@vue/test-utils";

/**
 * Random list of diacritic characters of various types
 */
export const TEST_DIACRITICS = ['Ù', 'À', 'à', 'ì', 'Ó', 'Í', 'ó', 'é', 'Î', 'ú', 'â', 'Ô', 'ô', 'Õ', 'õ', 'ñ', 'Ÿ', 'ä', 'ö', 'ÿ'];

/**
 * Returns a promise to a point where all the previous messages in the JavaScript message queue have
 * been processed.
 *
 * @returns Empty Promise
 */
export function flushQueue() {
  // setTimeout pushes a message onto the end of the message queue. Therefore when the setTimeout
  // gets resolved then the message queue must have processed all the previous messages.
  return new Promise((resolve) => setTimeout(resolve, 0));
}

/**
 * Reinterprets the input argument as a jest mock.
 *
 * @param func Function that is mocked
 * @returns Input argument interpreted as a jest mock
 */
export function castMock<T, Y extends any[]>(func: (...args: Y) => T) {
  if (!jest.isMockFunction(func)) {
    throw new Error('Argument is not jest mock');
  }
  return <jest.Mock<T, Y>>func;
}

/**
 * Gets todays date and adds on a certain number of years
 *
 * @param years the number of years to add onto today
 * @returns Todays date with x more years
 */
export function todayPlusYears(years: number): string {
  let today = new Date();
  let currentYears = today.getFullYear() + years;
  return `${currentYears}-${today.getMonth()}-${today.getDate()}`;
}

/**
 * Creates a test business with the given business id
 *
 * @param businessId The business id to use
 * @param administrators The administrator ids to administer this business
 * @returns The generated business
 */
export function makeTestBusiness(businessId: number, administrators?: number[]): Business {
  let business: Business = {
    id: businessId,
    primaryAdministratorId: 1,
    name: 'test_business_name' + businessId,
    address: { country: 'test_business_country' + businessId },
    description: 'test_business_description' + businessId,
    created: '1/5/2005',
    businessType: 'Accommodation and Food Services',
    points: 5,
    rank: {
      name: 'bronze',
    }
  };

  if (administrators !== undefined) {
    business.administrators = administrators.map(userId => makeTestUser(userId));
  }
  return business;
}

/**
 * Creates a test user with the given user id
 *
 * @param userId The user id to use
 * @param businesses The businesses for this user to administer
 * @param role Role of the user
 * @returns The generated user
 */
export function makeTestUser(userId: number, businesses?: number[], role?: UserRole): User {
  let user: User = {
    id:  userId,
    firstName: 'test_firstname' + userId,
    lastName: 'test_lastname' + userId,
    nickname: 'test_nickname' + userId,
    email: 'test_email' + userId,
    bio: 'test_biography' + userId,
    phoneNumber: 'test_phone_number' + userId,
    dateOfBirth: '1/1/1900',
    created: '1/5/2005',
    homeAddress: {
      streetNumber: 'test_street_number',
      streetName: 'test_street1',
      city: 'test_city',
      region: 'test_region',
      postcode: 'test_postcode',
      district: 'test_district',
      country: 'test_country' + userId
    },
    role: role ?? 'user',
    images: [],
  };
  if (businesses !== undefined) {
    user.businessesAdministered = businesses.map(businessId => makeTestBusiness(businessId));
  }
  return user;
}

/**
 * Finds a v-btn within the provided wrapper that contains some text
 *
 * @param wrapper Wrapper to find component within
 * @param text Button text to filter by
 * @returns Button that includes the given text or a wrapper with .exists() === false
 */
export function findButtonWithText(wrapper: Wrapper<any>, text: string): Wrapper<any> {
  const buttons = wrapper.findAllComponents({ name: "v-btn" });
  const filtered = buttons.filter((button) =>
    button.text().includes(text)
  );
  expect(filtered.length).toBeLessThanOrEqual(1); // Make sure there are no duplicates

  if (filtered.length === 1) {
    return filtered.at(0);
  } else {
    // If you know a better method for creating a non-existent wrapper let me know
    return wrapper.findComponent({ ref: 'very-non-existent-selector'});
  }
}