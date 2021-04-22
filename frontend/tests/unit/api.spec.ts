import axios, { AxiosError, AxiosInstance } from 'axios';

import * as api from '@/api';
import { AxiosResponse } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function() {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    post: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
    put: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends {[k: string]: (...args: any[]) => any}> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>>}

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'post' | 'patch' | 'delete' | 'put'>> = axios.instance;

/**
 * Wraps a value inside an AxiosResponse.
 *
 * @param value The value inside the AxiosResponse
 * @param status The status for the response, if not provided then 200 is used
 * @returns An axios response with the value inside
 */
function makeAxiosResponse<T>(value: T, status = 200): AxiosResponse<T> {
  return {
    data: value,
    status: status,
    statusText: 'Status ' + status,
    headers: {},
    config: {},
  };
}

/**
 * Makes an axios error corresponding to a request with no response.
 *
 * @returns An axios error with an empty response field.
 */
function makeNoResponseError(): AxiosError<unknown> {
  return {
    name: 'test_no_response_name',
    message: 'test_no_response_message',
    isAxiosError: true,
    config: {},
    toJSON() {
      throw new Error('Not implemented');
    }
  };
}

/**
 * Creates an axios error with a value
 *
 * @param value The value inside the response.data field.
 * @param status The status code for this error
 * @returns An axios error
 */
function makeAxiosError<T>(value: T, status = 400): AxiosError<T> {
  return {
    name: 'test_error_name',
    message: 'test_error_message',
    isAxiosError: true,
    config: {},
    response: makeAxiosResponse(value, status),
    toJSON() {
      throw new Error('Not implemented');
    }
  };
}

// Creates a type from the methods of T where the methods return promises
type PromiseMethods<T> = Pick<T, ({[P in keyof T]: T[P] extends (...args: any[]) => Promise<any> ? P : never })[keyof T]>;

// Creates a type where the promise around T is removed
type UnwrapPromise<T extends Promise<any>> = T extends Promise<infer U> ? U : never;

// All the api methods in api.ts
type ApiMethods = PromiseMethods<typeof api>;

// Object representing a call to an api method and the results of said call
type ApiCall = {[k in keyof ApiMethods]: {
  methodName: k,                                    // The method name to call
  parameters: Parameters<ApiMethods[k]>,            // The arguments that are provided
  httpMethod: keyof (typeof instance),              // The HTTP method to be used
  url: string,                                      // The expected url to be accessed
  body: any,                                        // The expected http body
  result: UnwrapPromise<ReturnType<ApiMethods[k]>>, // The expected function result if successful
  apiResult?: any,                                  // If the api returns a different value from the overall result then this should be used to specify that
}}[keyof ApiMethods];

const apiCalls: ApiCall[] = [
  {
    methodName: 'createProduct',
    parameters: [
      7,
      {
        id: 'ID-VALUE',
        name: 'test_name',
        description: 'test_description',
        manufacturer: 'test_manufacturer',
        recommendedRetailPrice: 100,
      }
    ],
    httpMethod: 'post',
    url: '/businesses/7/products',
    body: {
      id: 'ID-VALUE',
      name: 'test_name',
      description: 'test_description',
      manufacturer: 'test_manufacturer',
      recommendedRetailPrice: 100,
    },
    result: undefined,
  },
  {
    methodName: 'createUser',
    parameters: [
      {
        firstName: 'test_firstname',
        lastName: 'test_lastname',
        middleName: 'test_middlename',
        nickname: 'test_nickname',
        bio: 'test_bio',
        email: 'test_email',
        dateOfBirth: 'test_date_of_birth',
        phoneNumber: 'test_phone_number',
        homeAddress: { country: 'test_country'},
        password: 'test_password',
      }
    ],
    httpMethod: 'post',
    url: '/users',
    body: {
      firstName: 'test_firstname',
      lastName: 'test_lastname',
      middleName: 'test_middlename',
      nickname: 'test_nickname',
      bio: 'test_bio',
      email: 'test_email',
      dateOfBirth: 'test_date_of_birth',
      phoneNumber: 'test_phone_number',
      homeAddress: { country: 'test_country'},
      password: 'test_password',
    },
    result: undefined,
  },
  {
    methodName: 'login',
    parameters: ['test_email', 'test_password'],
    httpMethod: 'post',
    url: '/login',
    body: { email: 'test_email', password: 'test_password' },
    result: 7,
    apiResult: { userId: 7 },
  }
];

// Makes the stringification in the describe.each block look a bit nicer
apiCalls.map(call =>
  Object.assign(call, { toString(): string { return (this as any).methodName; }})
);

// Makes sure the the mocks are clean
afterEach(() => {
  jest.clearAllMocks();
});

describe('api', () => {
  describe.each(apiCalls)('"%s" method', (fields) => {

    function doCall() {
      // @ts-ignore - This should pass type checking but doesn't :(
      return api[fields.methodName](...fields.parameters);
    }

    it('If the backend is unavailable then there should be a "Failed to reach backend" message', async () => {
      instance[fields.httpMethod].mockRejectedValueOnce(makeNoResponseError());

      let response = await doCall();

      expect(response).toBe('Failed to reach backend');
    });

    it('If an unknown status is returned then response should be "Request failed: ?"', async () => {
      const statusCode = 875;
      instance[fields.httpMethod].mockRejectedValueOnce(makeAxiosError(undefined, statusCode));

      let response = await doCall();

      expect(response).toBe('Request failed: ' + statusCode);
    });

    it('Method should be called with the expected url and body', async () => {
      let apiResult = fields.apiResult;
      if (fields.apiResult === undefined) apiResult = fields.result;
      instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(apiResult));

      await doCall();

      expect(instance[fields.httpMethod]).toBeCalledWith(fields.url, fields.body);
    });

    it('Method should return the correct value if successful', async () => {
      let apiResult = fields.apiResult;
      if (fields.apiResult === undefined) apiResult = fields.result;
      instance[fields.httpMethod].mockResolvedValueOnce(makeAxiosResponse(apiResult));

      let response = await doCall();

      expect(response).toBe(fields.result);
    });
  });
});