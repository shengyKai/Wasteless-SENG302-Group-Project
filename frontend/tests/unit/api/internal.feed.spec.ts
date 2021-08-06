import * as api from '@/api/internal';
import axios, { AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }),
  instance: {
    delete: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'delete' >> = axios.instance;

describe("Test DELETE /feed/delete/{id} endpoint", () => {
  it("Notification is deleted successfully, no response", async () => {
    instance.delete.mockResolvedValueOnce({
      response: {
        status: 200
      }
    });
    const response = await api.deleteNotification(1);
    expect(response).toEqual(undefined);
  });

  it("No auth token is passed with request, user is informed they are logged out and the notification is not deleted", async () => {
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await api.deleteNotification(1);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it("Notification exists but is not on your feed, unauthorised to delete notification", async () => {
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await api.deleteNotification(1);
    expect(response).toEqual("Invalid authorization for notification removal");
  });

  it("Notification with given ID cannot be accessed, not found returned", async () => {
    instance.delete.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await api.deleteNotification(1);
    expect(response).toEqual("Notification not found");
  });
});