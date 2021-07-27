import { MarketplaceCardSection } from "./api/internal";

/**
 * Sets a cookie. Will expire in one year.
 * @param name Name of the cookie being set.
 * @param value Value to store in the cookie.
 */
export function setCookie(name: string, value: string|number) {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  document.cookie = `${name}=${value}; expires=${date.toUTCString()}; path=/`;
}

/**
 * Reads a cookie.
 * @param name Name of the cookie to read.
 * @returns Value of the cookie. NULL if the cookie does not exist.
 */
export function getCookie(name: string) {
  let cookies = document.cookie.split(';');
  for (let cookie of cookies) {
    cookie = cookie.trim();
    if (cookie.startsWith(name + '=')) {
      return cookie;
    }
  }
  return null;
}

/**
 * Deletes a cookie.
 * @param name Name of the cookie to delete.
 */
export function deleteCookie(name: string) {
  document.cookie = `${name}=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;`;
}

/**
 * Cookie names.
 */
export const COOKIE = {
  USER: 'user'
};

/**
 * Converts a date to a human readable string
 *
 * @param date Date to format, either a date object or a parsable date string
 * @returns Date in a human readable string
 */
export function formatDate(date: Date | string) {
  if (typeof date === 'string') date = new Date(date);
  const parts = date.toDateString().split(' ');
  return `${parts[2]} ${parts[1]} ${parts[3]}`;
}

export function formatPrice(price : number) {
  if (Number.isInteger(price)) {
    return price.toString();
  } else {
    return (+price).toFixed(2);
  }
}

/**
 * User roles.
 */
export const USER_ROLES = {
  USER: 'user',
  GAA: 'globalApplicationAdmin',
  DGAA: 'defaultGlobalApplicationAdmin'
};

/**
 * Mapping between section key and section name
 */
export const SECTION_NAMES: Record<MarketplaceCardSection, string> = {
  ForSale: 'For Sale',
  Wanted: 'Wanted',
  Exchange: 'Exchange',
};


/**
 * Rate limits the input function by waiting a given amount of time calling the inner function.
 * If interrupted with another call then this wait is reset.
 *
 * @param func Function to rate limit
 * @param wait Time (ms) to wait before calling the function
 */
export function debounce(func: (() => void), wait: number) {
  let timeout: number | undefined;
  function debounced() {
    if (timeout !== undefined) {
      clearTimeout(timeout);
    }
    timeout = setTimeout(func, wait);
  }
  return debounced;
}

/**
 * Trims the input string such that it is always less than or equal to the provided length
 * This function will try to avoid breaking up words unless there is no alternative
 * @param str String to trim
 * @param length Maximum length
 * @returns Trimmed string
 */
export function trimToLength(str: string, length: number) {
  const re = new RegExp(`^(.{0,${length}})(\\s.*|$)`);
  return str.replace(re, "$1").slice(0, length);
}

/**
 * Checks whether we are executing in the test enviroment
 *
 * @returns true if we're testing false otherwise
 */
export function isTesting() {
  return process.env.JEST_WORKER_ID !== undefined;
}

/**
 * ========= REGEX =========
 */

/**
 * Returns a Regex pattern that matches letters, spaces, dashes, hyphen and apostrophe
 */
export function regxAlphabet() {
  return /^[\p{L}\-.' ]*$/u;
}

/**
 * Returns a Regex pattern that matches letters, spaces, numbers and common characters
 */
export function regxAlphabetExtended() {
  return /(^[\p{L}\d\p{P} ]*$)/u;
}

export function regxAlphabetExtendedMultiline() {
  return /(^[\p{L}\d\p{P}$\s]*$)/u;
}

/**
 * Returns a Regex pattern that matches numbers
 */
export function regxNumerical() {
  return /(^[\d]*$)/;
}

/**
 * Returns a Regex pattern that matches a price
 */
export function regxPrice() {
  return /(^\d{1,8}(\.\d{2})?$)|^$/;
}

/**
 * Returns a Regex that matches a password with at least one number and one letter.
 */
export function regxPassword() {
  return /^(?=.*[0-9])(?=.*[\p{L} ])([\p{L}0-9 !@#$%^&*()-]+)$/u;
}

/**
 * Returns a Regex that matches a valid email address
 */
export function regxEmail() {
  return /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
}

/**
 * Returns a Regex that matches a valid post code
 */
export function regxPostCode() {
  return /(^[\p{L}0-9]*$)/u;
}

/**
 Returns a Regex that matches a valid phone number
 */
export function regxPhoneNumber() {
  return /(^\(?\d{1,3}\)?[\s.-]?\d{3,4}[\s.-]?\d{4,5}$)|(^$)/;
}

/**
 Returns a Regex that matches a country code
 */
export function regxCountryCode() {
  return /(^(\d{1,2}-)?\d{2}$)|(^$)/;
}
/**
 Returns a Regex that matches a valid street address
 */
export function regxStreet() {
  return /^(([0-9]+|[0-9]+\/[0-9]+)[\p{L}]?)(?=\s[\p{L}])([\p{L}0-9-.' ]+)$/u;
}
/**
 Returns a Regex that matches a valid product code
 */
export function regxProductCode() {
  return /^[-A-Z0-9]+$/;
}

/**
 * ========= RULES =========
 */

export const emailRules = [
  (email: string) => regxEmail().test(email) || 'E-mail must be valid'
];

export const mandatoryRules = [
  //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
  //if it does not follow the format, display error message
  (field: string) => !!field || 'Field is required'
];

export const passwordRules = [
  (field: string) => (field && field.length >= 7) || 'Password must have 7+ characters',
  (field: string) => regxPassword().test(field) || 'Must have at least one number and one letter'
];

export const postCodeRules = [
  (field: string) => regxPostCode().test(field) || 'Must contain numbers and alphabet only'
];

export const nameRules = [
  (field: string) =>  (field.length === 0 || regxAlphabet().test(field)) || 'Naming must only contain letter and common characters'
];

export function maxCharRules(size: number) {
  return [
    (field: string) => (field.length <= size) || `Reached max character limit: ${size}`
  ];
}

export const phoneNumberRules = [
  (field: string) => regxPhoneNumber().test(field) || 'Must only contain 8-12 digits'
];
export const countryCodeRules = [
  (field: string) => regxCountryCode().test(field) || 'Must only contain 2 digits.'
];
export const alphabetRules = [
  (field: string) => (field.length === 0 || regxAlphabet().test(field)) || 'Naming must be valid'
];
export const streetNumRules = [
  (field: string) => (field && field.length <= 109) || 'Reached max character limit 109 ',
  (field: string) => regxStreet().test(field) || 'Must contain unit number and street name'
];

export const quantityRules = [
  (field: string) => regxNumerical().test(field) || 'Must contain an integer',
  (field: string) => field ? parseInt(field) > 0 || 'Must be greater than zero' : true
];

export const smallPriceRules = [
  //A price must be numbers and may contain a decimal followed by exactly two numbers (4digit)
  (field: string) => regxPrice().test(field) || 'Must be a valid price',
  (field: string) => field ? parseInt(field) < 10000 || 'Must be less than 10,000' : true,
];

export const hugePriceRules = [
  //A price must be numbers and may contain a decimal followed by exactly two numbers (6digit)
  (field: string) => regxPrice().test(field) || 'Must be a valid price',
  (field: string) => field ? parseInt(field) < 1000000 || 'Must be less than 1,000,000' : true
];

export const alphabetExtendedSingleLineRules = [
  (field: string) => regxAlphabetExtended().test(field) || 'Must only contain letters, numbers, punctuation and spaces',
];

export const alphabetExtendedMultilineRules = [
  (field: string) => regxAlphabetExtendedMultiline().test(field) || 'Must only contain letters, numbers, punctuation and whitespace',
];

export const productCodeRules = [
  (field: string) => field.length <= 15 || 'Reached max character limit: 15',
  (field: string) => !/ /.test(field) || 'Must not contain a space',
  (field: string) => regxProductCode().test(field) || 'Must be all uppercase letters, numbers and dashes.',
];
