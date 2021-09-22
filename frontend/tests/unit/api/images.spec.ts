import { uploadImage } from "@/api/images";
import { Image } from "@/api/internal";
import axios, { AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    post: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'post' >> = axios.instance;


describe('POST /media/images endpoint', () => {
  const testFile = new File([], 'test_file');

  it.only('When the API request is successfully resolved, response image is returned', async ()=>{
    const image: Image = {
      id: 69,
      filename: "cremebrulee.png",
      thumbnailFilename: "spagettiboligne.jpg"
    };
    /*instance.post.mockResolvedValueOnce({
      response: {
        status: 201,
        data: image
      }
    });*/

    instance.post.mockImplementationOnce(() => console.log('called') as any);

    const response = await uploadImage(testFile);
    expect(response).toEqual(image);
  });

  it('When the API request has a 201 response code an invalid response format, error message saying image was not received is returned', async ()=>{
    const image = {
      filename: "cremebrulee.png",
      thumbnailFilename: "spagettiboligne.jpg"
    };
    instance.post.mockResolvedValueOnce({
      response: {
        status: 201,
        data: image
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("Image was not received");
  });

  it('When the response is 400, a message says image is invalid', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 400,
        data: {
          messsage: "Lasagne"
        }
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("Invalid image: Lasagne");
  });

  it('When the session is invalid, message teeling user to log back in', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 401
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("You have been logged out. Please login again and retry");
  });

  it('When the user has invalid permission, permission error', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 403
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("Operation not permitted");
  });

  it('When image too large, image too large error', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 413
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("Image too large");
  });

  it('When response is undefined, failed to reach backend', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: undefined
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("Failed to reach backend");
  });

  it('Any other response, message should be displayed', async ()=>{
    instance.post.mockRejectedValueOnce({
      response: {
        status: 123,
        data: {
          message: "message from server"
        }
      }
    });
    const response = await uploadImage(testFile);
    expect(response).toEqual("Request failed: message from server");
  });

});