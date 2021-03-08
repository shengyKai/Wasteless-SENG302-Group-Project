export function setCookie(name: string, value: string|number) {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  document.cookie = `${name}=${value}; expires=${date.toUTCString()}; path=/`;
}

export function getCookie(name: string) {
  let cookies = document.cookie.split(';');
  let target = null;
  cookies.forEach(cookie => {
    if (cookie.startsWith(`${name}=`)) target = cookie;
  });
  return target;
}

export function deleteCookie(name: string) {
  document.cookie = `${name}=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;`;
}

export const COOKIE = {
  USER: 'USER'
}

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