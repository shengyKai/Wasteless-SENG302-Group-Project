import axios, { AxiosInstance } from 'axios';
import {ModifyUser, modifyUser} from "@/api/internal-user";

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

describe('Test PUT /users/{userId} endpoint', () => {
  let user: ModifyUser = {
    firstName: "John",
    lastName: "Smith",
    phoneNumber: "123456789",
    email: "john.smith@gmail.com",
    password: "password123",
    newPassword: "123password",
    dateOfBirth: "01-01-2000",
    homeAddress: {
      streetNumber: "123",
      streetName: "Some Street",
      city: "Christchurch",
      region: "Canterbury",
      country: "New Zealand"
    }
  };

  it('When API request is successfully resolved, returns undefined', async () => {
    instance.put.mockResolvedValueOnce({
      response: {
        status: 200,
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual(undefined);
  });

  it('When API request is unsuccessful and gives an undefined error, returns a message stating failed to reach backend', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: undefined
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual("Failed to reach backend");
  });

  it('When API request is unsuccessful and gives a 401 error, returns a message saying the details are invalid along with a reason from the backend', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          message: 'Name too long',
        }
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual("Invalid details entered: Name too long");
  });

  it('When API request is unsuccessful and gives a 401 error, returns a message stating user has been logged out', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When API request is unsuccessful and gives a 403 error, returns a message stating user cannot be updated with a reason from the backend', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 403,
        data: {
          message: "Incorrect password"
        }
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual("Cannot update user: Incorrect password");
  });

  it('When API request is unsuccessful and gives a 406 error, returns a message saying the user does not exist', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 406
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual("User does not exist");
  });


  it('When API request is unsuccessful and gives an uncaught error status, returns a message stating that error status and a message', async () => {
    instance.put.mockRejectedValueOnce({
      response: {
        status: 999,
        data: {
          message: "some error"
        }
      }
    });
    const response = await modifyUser(144, user);
    expect(response).toEqual("Request failed: some error");
  });
});