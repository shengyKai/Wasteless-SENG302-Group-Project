import axios from 'axios';
import { is } from 'typescript-is';
import { MaybeError, instance } from './internal';

export type Image = {
  id: number,
  filename: string,
  thumbnailFilename: string,
};

/**
 * Upload an image to the application. Response will contain the location of the uploaded image and its associated id.
 * @param image The expected image file
 */
export async function uploadImage(image: File): Promise<MaybeError<Image>> {
  try {
    let formData = new FormData();
    formData.append('file', image);
    const response = await instance.post(`/media/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    if (!is<Image>(response.data)) {
      return 'Image was not received';
    }
    return response.data;
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 400) return 'Invalid image: ' + error.response?.data.message;
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 403) return 'Operation not permitted';
    if (status === 413) return 'Image too large';
    return 'Request failed: ' + error.response?.data.message;
  }
}