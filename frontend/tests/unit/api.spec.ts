import axios, { AxiosError, AxiosInstance } from 'axios';

import { CreateProduct, createProduct } from "@/api";
import { castMock } from './utils';
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

// Makes a type from T with only the methods of T
type Methods<T> = Pick<T, ({[P in keyof T]: T[P] extends (...args: any[]) => any ? P : never })[keyof T]>;

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Methods<AxiosInstance>> = axios.instance;

// Makes sure that the api.ts has created a axios instance.
expect(castMock(axios.create)).toBeCalled();

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

describe('api', () => {
  it('Successfully create product should return undefined', async () => {
    instance.post.mockResolvedValue(makeAxiosResponse(undefined)); // Set successful return value

    const product: CreateProduct = {
      id: 'ID-VALUE',
      name: 'test_name',
      description: 'test_description',
      manufacturer: 'test_manufacturer',
      recommendedRetailPrice: 100,
    };
    let result = await createProduct(7, product);

    expect(result).toBe(undefined);
    expect(instance.post).toBeCalledWith('/businesses/7/products', product);
  });
});