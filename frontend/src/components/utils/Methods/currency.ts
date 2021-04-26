import { MaybeError } from "@/api";

/**
 * Currency information for a specific country.
 */
export type Currency = {
    code: string,
    name: string,
    symbol: string
};

/**
 * Country information returned by API. Should only have field 'currencies' since request restricts fields to currencies.
 */
type Country = {
    currencies: Currency[];
};

/**
 * Determines if the given object is an instance of Currency.
 * @param obj The object to be checked.
 * @returns True if the object has the attributes of a Currency object, false otherwise.
 */
function isCurrency(obj : any): obj is Currency {
  if (obj === null || typeof obj !== 'object') return false;
  if (typeof obj.code !== 'string') return false;
  if (typeof obj.name !== 'string') return false;
  if (typeof obj.symbol !== 'string') return false;
  return true;
}

/**
 * Determines if the given object is an array of object of type Currency.
 * @param obj The object to be checked.
 * @returns True if the object is an array where all elements are Currency objects, false otherwise.
 */
function isCurrencyArray(obj: any): obj is Currency[] {
  if (!Array.isArray(obj)) return false;
  for (let elem of obj) {
    if (!isCurrency(elem)) return false;
  }
  return true;
}

/**
 * Check that the body of the API response has the expected format. It should be an array of length 1, where they
 * entry in the array is a JSON with a "currencies" field, and that field contains an array of currency objects.
 * @param response The body of the response recieved from the API.
 * @returns True if the response is in the expected format, false otherwise.
 */
function currencyResponseHasExpectedFormat(response: any): response is Country[] {
  if (!Array.isArray(response)) return false;
  if (response.length !== 1) return false;
  const country = response[0];
  if (!isCurrencyArray(country.currencies)) return false;
  return true;
}

/**
 * Default currency when currency of current location cannot be resolved from API request.
 */
export const newZealandDollar : Currency = {
  code: "NZD",
  name: "New Zealand dollar",
  symbol: "$"
};

/**
 * Make a request to the RESTCounties API to find the currency associated with the given country name.
 * If the request is successful a Currency object will be returned. If it is unsuccessful then a string
 * with an error message will be returned. If more than one currency is returned by the API, the first
 * one in the array will be returned.
 * @param country The name of a country to use in the API request for the currency.
 * @returns A promise which will be resolved into a string error message or a Currency object.
 */
export async function currencyFromCountry(country: string) : Promise<MaybeError<Currency>> {

  const queryUrl = `https://restcountries.eu/rest/v2/name/${country}?fullText=true&fields=currencies`;
  const response = await fetch(queryUrl);

  if (response.status === undefined) {
    throw `Failed to reach ${queryUrl}`;
  }
  if (response.status === 404) {
    throw `No country with name ${country} was found`;
  }
  if (response.status !== 200) {
    throw `Request failed: ' + ${response.status}`;
  }

  const data = await response.json();

  if (!currencyResponseHasExpectedFormat(data)) {
    throw 'Response was not in readable format';
  }

  return data[0].currencies[0];
}