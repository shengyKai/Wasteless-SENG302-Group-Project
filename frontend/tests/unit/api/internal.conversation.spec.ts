import axios, {AxiosInstance} from 'axios';
import {messageConversation} from "@/api/internal-event";

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    post: jest.fn()
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'post'>> = axios.instance;

describe("Test POST /cards/{cardId}/conversations/{buyerId} endpoint", () => {
  it("when the endpoint is called with valid parameters, a 201 response is returned", async () => {
    instance.post.mockResolvedValueOnce({
      response: {
        status: 201
      }
    });
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual(undefined);
  });

  it('No auth token is present in request, message to user is returned telling them to log in', async () =>{
    instance.post.mockRejectedValue({
      response: {
        status: 401
      }
    });
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('Owner tries to message self or when a user who is neither the creator of the card, card buyer, GAA or DGAA' +
    'tries to send add a message to the conversation. Permission denied error', async () =>{
    instance.post.mockRejectedValue({
      response: {
        status: 403
      }
    });
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual('You do not have permission to edit this conversation');
  });

  it('The card or given buyerId does not exists, card does not exist error', async () =>{
    instance.post.mockRejectedValue({
      response: {
        status: 406
      }
    });
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual("Unable to post message, the card does not exist");
  });

  it('The card or given buyerId does not exists, card does not exist error', async () =>{
    instance.post.mockRejectedValue({
      response: {
        status: 406
      }
    });
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual("Unable to post message, the card does not exist");
  });

  it('The response is undefined, backend not reached error', async () =>{
    instance.post.mockRejectedValue({});
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual("Failed to reach backend");
  });

  it('The response is 404, the exact error message is returned', async () =>{
    instance.post.mockRejectedValue({
      response: {
        status: 404,
        data: {
          message: "too many characters dude"
        }
      }
    });
    const response = await messageConversation(1, 1, 1, "");
    expect(response).toEqual("Request failed: too many characters dude");
  });
});