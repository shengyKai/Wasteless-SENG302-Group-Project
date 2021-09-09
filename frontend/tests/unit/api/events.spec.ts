import axios, { AxiosInstance } from 'axios';
import {AnyEvent, getEvents, updateEventAsRead} from '@/api/events';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    put: jest.fn(),
    get: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get' | 'put' >> = axios.instance;

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

describe('Test GET /users/:id/feed endpoint', () => {

  it('When getEvents is called with undefined modifiedSince date, GET /users/:id/feed endpoint is called with given user ID and no query parameters', async () => {
    instance.get.mock.calls = [];
    await getEvents(3, undefined);
    expect(instance.get.mock.calls.length).toBe(1);
    expect(instance.get.mock.calls[0][0]).toBe('/users/3/feed');
    expect(instance.get.mock.calls[0][1]).toStrictEqual({params: new URLSearchParams()});
  });

  it('When getEvents is called with a modifiedSince date, GET /users/:id/feed endpoint is called with given user ID and the given date as a query parameters', async () => {
    instance.get.mock.calls = [];
    const expectedParams = new URLSearchParams();
    expectedParams.append("modifiedSince", "2021-09-09 12:30:25.902Z");
    await getEvents(3, "2021-09-09 12:30:25.902Z");
    expect(instance.get.mock.calls.length).toBe(1);
    expect(instance.get.mock.calls[0][0]).toBe('/users/3/feed');
    expect(instance.get.mock.calls[0][1]).toStrictEqual({params: expectedParams});
  });

  it('When response has 200 status and is a valid event array, getEvents returns the event array', async () => {
    const responseData: AnyEvent[] = [
      {
        type: 'GlobalMessageEvent',
        status: 'normal',
        id: 1,
        tag: 'none',
        created: new Date().toString(),
        message: 'Event 1',
        read: false,
        lastModified: new Date().toString()
      },
      {
        type: 'GlobalMessageEvent',
        status: 'normal',
        id: 2,
        tag: 'none',
        created: new Date().toString(),
        message: 'Event 2',
        read: false,
        lastModified: new Date().toString()
      },
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const events = await getEvents(1, undefined);
    expect(events).toEqual(responseData);
  });

  it('When response has 200 status and is an invalid event array, getEvents returns a message saying the response is invalid', async () => {
    const responseData: any = [
      {
        type: 'GlobalMessageEvent',
        status: 'normal',
        id: 1,
        tag: 'none',
        created: new Date().toString(),
        message: 'Event 1',
        read: false,
        lastModified: new Date().toString()
      },
      {
        type: 'GlobalMessageEvent',
        status: 'normal',
        tag: 'none',
        created: new Date().toString(),
        message: 'Event 2',
        read: false,
        lastModified: new Date().toString()
      },
    ];
    instance.get.mockResolvedValueOnce({
      data: responseData
    });
    const events = await getEvents(1, undefined);
    expect(events).toEqual('Response is not an event array');
  });

  it('When response has a 400 status, getEvents returns an error message indicating that the modified date is incorrect', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 400,
      }});
    const message = await getEvents(1, undefined);
    expect(message).toEqual('Invalid \'modified since\' date');
  });

  it('When response has a 401 status, getEvents returns an error message indicating that the user is not logged in', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 401,
      }});
    const message = await getEvents(1, undefined);
    expect(message).toEqual('You have been logged out. Please login again and retry');
  });

  it('When response has a 403 status, getEvents returns an error message indicating that the user cannot view this user\'s events', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 403,
      }});
    const message = await getEvents(1, undefined);
    expect(message).toEqual('Invalid authorisation for getting events associated with user 1');
  });

  it('When response has a 406 status, getProducts returns an error message indicating that buesiness does not exists', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 406,
      }});
    const message = await getEvents(1, undefined);
    expect(message).toEqual('User does not exist');
  });

  it('When a response without a status is received, getProducts returns an error message indicating that the server could not be reached', async () => {
    instance.get.mockRejectedValueOnce("Server is down");
    const message = await getEvents(1, undefined);
    expect(message).toEqual('Failed to reach backend');
  });

  it('When response has an error status that is not 200, 400, 401, 403 or 406, getProducts will return the error message from the response', async () => {
    instance.get.mockRejectedValueOnce({
      response: {
        status: 732,
        data: {
          message: "You done messed up"
        }
      }});
    const message = await getEvents(1, undefined);
    expect(message).toEqual('Request failed: You done messed up');
  });

});