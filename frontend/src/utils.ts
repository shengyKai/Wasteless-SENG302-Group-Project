export function setCookie(name: string, value: string|number) {
  const date = new Date();
  date.setFullYear(date.getFullYear() + 1);
  document.cookie = `${name}=${value}; expires=${date.toUTCString()}; path=/`;
}

export function getCookie(name: string) {
  var cookies = document.cookie.split(';');
  var target = null;
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