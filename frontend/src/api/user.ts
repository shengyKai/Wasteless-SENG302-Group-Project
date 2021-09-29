import {is} from 'typescript-is';
import {Location, MaybeError, SearchResults, instance} from "@/api/internal";
import {Business} from "@/api/business";
import {Image} from "@/api/images";

export type UserRole = "user" | "globalApplicationAdmin" | "defaultGlobalApplicationAdmin"
type UserOrderBy = 'userId' | 'relevance' | 'firstName' | 'middleName' | 'lastName' | 'nickname' | 'email';

export type User = {
  id: number,
  firstName: string,
  lastName: string,
  middleName?: string,
  nickname?: string,
  bio?: string,
  email: string,
  dateOfBirth?: string,
  phoneNumber?: string,
  homeAddress: Location,
  created?: string,
  role?: UserRole,
  businessesAdministered?: Business[],
  images: Image[],
};

export type BaseUser = {
  firstName: string,
  lastName: string,
  middleName?: string,
  nickname?: string,
  bio?: string,
  email: string,
  dateOfBirth: string,
  phoneNumber?: string,
  homeAddress: Location,
};

export type CreateUser = BaseUser & {
  password: string,
};

export type ModifyUser = BaseUser & {
  password?: string,
  newPassword?: string,
  imageIds: number[]
}

/**
 * Sends a search query to the backend.
 *
 * @param query Query string to search for
 * @param pageIndex Index of page to start the results from (1 = first page)
 * @param resultsPerPage Number of results to return per page
 * @param orderBy Specifies the method used to sort the results
 * @param reverse Specifies whether to reverse the search results (default order is descending for relevance and ascending for all other orders)
 * @returns List of user infos for the current page or an error message
 */
export async function userSearch(query: string, pageIndex: number, resultsPerPage: number, orderBy: UserOrderBy, reverse: boolean): Promise<MaybeError<SearchResults<User>>> {
  let response;
  try {
    response = await instance.get('/users/search', {
      params: {
        searchQuery: query,
        page: pageIndex,
        resultsPerPage,
        orderBy,
        reverse: reverse.toString(),
      }
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${error.response.data.message}`;
  }

  if (!is<SearchResults<User>>(response.data)) {
    return 'Response is not user array';
  }

  return response.data;
}

/**
 * Queries the backend for a specific user by their id.
 *
 * @param id User id, if ommitted then fetch the logged in user's info
 * @returns User info for the given id or an error message
 */
export async function getUser(id: number): Promise<MaybeError<User>> {
  let response;
  try {
    response = await instance.get('/users/' + id);
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    return `Request failed: ${status}`;
  }

  if (!is<User>(response.data)) {
    return 'Response is not user';
  }

  return response.data;
}

/**
 * Logs in to the given user account by setting the authentication cookie.
 *
 * @param email User email
 * @param password User password
 * @returns The now logged in user ID if operation is successful, otherwise a string error.
 */
export async function login(email: string, password: string): Promise<MaybeError<number>> {
  let response;
  try {
    response = await instance.post('/login', {
      email: email,
      password: password,
    });
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid credentials';
    return `Request failed: ${status}`;
  }
  let id = response.data.userId;
  if (!is<number>(id)) return 'Invalid response';

  return id;
}

/**
 * Creates a user with the given properties.
 * Note: This doesn't automatically log the user in.
 *
 * @param user Initial user properties
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function createUser(user: CreateUser): Promise<MaybeError<undefined>> {
  try {
    await instance.post('/users', user);
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 409) return 'Email in use';
    return error.response.data?.message;
  }

  return undefined;
}

/**
 * Update the parameters of a user by sending a request with the new parameters to the backend.
 * Will return undefined if the request is successful, or a string error message explaining why
 * the request failed if it is unsuccessful.
 * @param userId The id number of the user to be updated.
 * @param user Details to update user with
 * @returns Undefined if request succeeds or a string error message if it does not.
 */
export async function modifyUser(userId: number, user: ModifyUser): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/users/${userId}`, user);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid details entered: ' + error.response?.data.message;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Cannot update user: ' + error.response?.data.message;
    if (status === 406) return 'User does not exist';
    return 'Request failed: ' + error.response?.data.message;
  }
  return undefined;
}

/**
 * Makes the given user a GAA, if the current user is a DGAA.
 *
 * @param userId User to make GAA
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function makeAdmin(userId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/users/${userId}/makeAdmin`);
  } catch (error) {
    let status: number | undefined = error.response?.status;

    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'User does not exist';

    return 'Request failed: ' + status;
  }
  return undefined;
}

/**
 * Revokes the given user's admin rights
 *
 * @param userId User to revoke permissions
 * @returns undefined if operation is successful, otherwise a string error.
 */
export async function revokeAdmin(userId: number): Promise<MaybeError<undefined>> {
  try {
    await instance.put(`/users/${userId}/revokeAdmin`);
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 406) return 'User does not exist';

    return 'Request failed: ' + status;
  }
  return undefined;
}