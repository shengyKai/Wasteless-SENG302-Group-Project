/*
 * Created on Wed Feb 10 2021
 *
 * The Unlicense
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or distribute
 * this software, either in source code form or as a compiled binary, for any
 * purpose, commercial or non-commercial, and by any means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors of this
 * software dedicate any and all copyright interest in the software to the public
 * domain. We make this dedication for the benefit of the public at large and to
 * the detriment of our heirs and successors. We intend this dedication to be an
 * overt act of relinquishment in perpetuity of all present and future rights to
 * this software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */

/**
 * Declare all available services here
 */
import axios from 'axios'  
  
const SERVER_URL = process.env.VUE_APP_SERVER_ADD;

const instance = axios.create({  
  baseURL: SERVER_URL,
  timeout: 2000  
});  

type MaybeError<T> = T | string;

type User = {
  id: number,
  firstName: string,
  lastName: string,
  middleName?: string,
  nickname?: string,
  bio?: string,
  email: string,
  dateOfBirth: string,
  phoneNumber?: string,
  homeAddress: string,
  created?: string,
  role: "user" | "globalApplicationAdmin" | "defaultGlobalApplicationAdmin",
};

function isUser(obj: any): obj is User {
  if (typeof obj.id !== 'number') return false;
  if (typeof obj.firstName !== 'string') return false;
  if (typeof obj.lastName !== 'string') return false;
  if (obj.middleName !== undefined && typeof obj.middleName !== 'string') return false;
  if (obj.nickname !== undefined && typeof obj.nickname !== 'string') return false;
  if (obj.bio !== undefined && typeof obj.bio !== 'string') return false;
  if (typeof obj.email !== 'string') return false;
  if (typeof obj.dateOfBirth !== 'string') return false;
  if (obj.phoneNumber !== undefined && typeof obj.phoneNumber !== 'string') return false;
  if (typeof obj.homeAddress !== 'string') return false;
  if (obj.created !== undefined && typeof obj.created !== 'string') return false;
  if (!['user', 'globalApplicationAdmin', 'defaultGlobalApplicationAdmin'].includes(obj.role)) return false;
  if (!isNumberArray(obj.businessesAdministered)) return false;

  return true;
}

function isNumberArray(obj: any): obj is number[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (typeof elem !== 'number') return false;
  }
  return true;
}

function isUserArray(obj: any): obj is User[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (!isUser(elem)) return false;
  }
  return true;
}
  
export default {  
  async search(query: string): Promise<MaybeError<User[]>> {
    let response;
    try {
      response = await instance.get('/users/search', {
        params: {
          'searchQuery': query,
        }
      });
    } catch (error) {
      return 'Request failed';
    }

    if (!isUserArray(response.data)) {
      return 'Response is not user';
    }

    return response.data;
  }
}