import {is} from 'typescript-is';
import {instance, MaybeError} from '@/api/internal';

export type ReportGranularity = 'daily' | 'weekly' | 'monthly' | 'yearly';


export type Record = {
  date: string,
  uniqueListingsSold?: number,
  uniqueBuyers?: number,
  uniqueProducts?: number,
  totalInterestCount?: number,
  averageInterestCount?: number,
  totalQuantitySold?: number,
  averageTimeToSell?: number,
  averageListingPrice?: number,
  totalValue?: number
}


/**
 * Queries the backend to generate a sales report for a business within a given time period and with given granularity
 * @param businessId The ID of the business to generate the sales report for.
 * @param startDate The date to generate sales from
 * @param endDate The date to generate sales to
 * @param granularity The granularity of the generated report. Can be one of: daily, weekly, monthly, yearly
 */
export async function generateReport(businessId: number, startDate: string, endDate: string, granularity: ReportGranularity): Promise<MaybeError<Record[]>> {
  let response;
  try {
    response = await instance.get(`/businesses/${businessId}/reports`, {data:{
      startDate,
      endDate,
      granularity
    }});
  } catch (error) {
    let status: number | undefined = error.response?.status;
    if (status === undefined) return 'Failed to reach backend';
    if (status === 401) return 'You have been logged out. Please login again and retry';
    if (status === 406) return 'Business not found';

    return 'Request failed: ' + status;
  }

  if(!is<Record[]>(response.data)) {
    return "Invalid response type";
  }

  return response.data;
}