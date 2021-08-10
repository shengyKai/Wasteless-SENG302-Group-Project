import { deleteNotification, setEventTag} from "@/api/internal";
import axios, { AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }),
  instance: {
    delete: jest.fn(),
    put: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'delete' | 'put'>> = axios.instance;

describe("Test DELETE /feed/{id} endpoint", () => {
  it("Notification is deleted successfully, no response", async () => {
    instance.delete.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await deleteNotification(1);
    expect(response).toEqual(undefined);
  });

  it("No auth token is passed with request, user is informed they are logged out and the notification is not deleted", async () => {
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await deleteNotification(1);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it("Notification exists but is not on your feed, unauthorised to delete notification", async () => {
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await deleteNotification(1);
    expect(response).toEqual("Invalid authorization for notification removal");
  });

  it("Notification with given ID cannot be accessed, not found returned", async () => {
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await deleteNotification(1);
    expect(response).toEqual("Notification not found");
  });
});

describe("Test PUT /feed/{eventid}/tag endpoint", () => {
  it("Event tag had been updated and there is no error from the respond", async () => {
    instance.put.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await setEventTag(1, 'red');
    expect(response).toEqual(undefined);
  });

  it("Missing Auth token, Inform user and the Event's tag will not be updated", async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await setEventTag(1, 'blue');
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it("Invalid authorization to tag the Event, Inform the user and the Event's tag will not be updated", async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await setEventTag(1, 'yellow');
    expect(response).toEqual('Invalid authorization for Event tagging');
  });

  it("Event ID is invalid, Inform the user and the Event's tag will not be updated ", async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await setEventTag(1, 'purple');
    expect(response).toEqual('Event not found');
  });

});