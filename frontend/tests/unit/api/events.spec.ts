import axios, { AxiosInstance } from 'axios';
import {updateEventAsRead} from '@/api/events';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    put: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'put' >> = axios.instance;

describe('Test PUT /feed/{eventId}/read endpoint', () => {
  it('When API request is successfully resolved, it returns undefined', async () => {
    instance.put.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await updateEventAsRead(1);
    expect(response).toEqual(undefined);
  });

  it('When API request is returns a 401 status, it returns an error message associated to that status', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await updateEventAsRead(1);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When API request is returns a 403 status, it returns an error message associated to that status', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 403,
      }
    });
    const response = await updateEventAsRead(1);
    expect(response).toEqual("Invalid authorization for marking this event");
  });

  it('When API request is returns a 406 status, it returns an error message associated to that status', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await updateEventAsRead(1);
    expect(response).toEqual("Event does not exist");
  });

  it('When API request is returns a 406 status, it returns an error message associated to that status', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: undefined
      }
    });
    const response = await updateEventAsRead(1);
    expect(response).toEqual('Failed to reach backend');
  });

  it('When API request is returns a 406 status, it returns an error message associated to that status', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 999,
        data: {
          message: "some error"
        }
      }
    });
    const response = await updateEventAsRead(1);
    expect(response).toEqual('Request failed: some error');
  });
});