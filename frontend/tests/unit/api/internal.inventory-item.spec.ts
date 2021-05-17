import * as api from '@/api/internal';
import axios, { AxiosError, AxiosInstance } from 'axios';
import {makeAxiosResponse, makeNoResponseError, makeAxiosError} from './api.spec';

jest.mock('axios', () => ({
  create: jest.fn(function() {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends {[k: string]: (...args: any[]) => any}> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>>}

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'post' | 'put' >> = axios.instance;

const testFile = new File([], 'test_file');
let testFormData = new FormData();
testFormData.append('file', testFile);

// Creates a type from the methods of T where the methods return promises
type PromiseMethods<T> = Pick<T, ({[P in keyof T]: T[P] extends (...args: any[]) => Promise<any> ? P : never })[keyof T]>;

// Creates a type where the promise around T is removed
type UnwrapPromise<T extends Promise<any>> = T extends Promise<infer U> ? U : never;

// All the api methods in api.ts
type ApiMethods = PromiseMethods<typeof api>;

// Object representing a call to an api method and the results of said call
type ApiCalls = {[k in keyof ApiMethods]: {
  parameters: Parameters<ApiMethods[k]>,            // The arguments that are provided
  httpMethod: keyof (typeof instance),              // The HTTP method to be used
  url: string,                                      // The expected url to be accessed
  body: any,                                        // The expected http body
  headers?: any,                                    // The expected headers
  result: UnwrapPromise<ReturnType<ApiMethods[k]>>, // The expected function result if successful
  apiResult?: any,                                  // If the api returns a different value from the overall result then this should be used to specify that
}};
