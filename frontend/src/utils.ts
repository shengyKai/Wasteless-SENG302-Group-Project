import vue from 'vue';

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

/**
 * User roles.
 */
export const USER_ROLES = {
  USER: 'user',
  GAA: 'globalApplicationAdmin',
  DGAA: 'defaultGlobalApplicationAdmin'
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
 * Object which allows current time to be updated at the same rate for all components.
 */
let synchronizedTime = vue.observable({now: new Date()});
setInterval(() => synchronizedTime.now = new Date(), 1000);

/**
 * Other components should not be able to modify the synchronized time object.
 */
Object.freeze(synchronizedTime);

export default synchronizedTime;