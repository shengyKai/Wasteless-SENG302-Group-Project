import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SalesReportPage from "@/components/BusinessProfile/SalesReport/SalesReportPage.vue";
import ReportOptionsBar from "@/components/BusinessProfile/SalesReport/ReportOptionsBar.vue";
import SalesReportTable from "@/components/BusinessProfile/SalesReport/SalesReportTable.vue";
import {getBusiness, Business} from "@/api/business";
import { User } from '@/api/user';
import {castMock} from './utils';
import {generateReport, SaleRecord} from '@/api/salesReport';
import axios, {AxiosInstance } from 'axios';

jest.mock('axios', () => ({
  create: jest.fn(function () {
    // @ts-ignore
    return this.instance;
  }
  ),
  instance: {
    get: jest.fn(),
  },
}));

// Makes a type for a mocked version of T where T is an object containing only methods.
type Mocked<T extends { [k: string]: (...args: any[]) => any }> = { [k in keyof T]: jest.Mock<ReturnType<T[k]>, Parameters<T[k]>> }

// @ts-ignore - We've added an instance attribute in the mock declaration that mimics a AxiosInstance
const instance: Mocked<Pick<AxiosInstance, 'get'>> = axios.instance;

Vue.use(Vuetify);

jest.mock('@/api/business', () => ({
  getBusiness: jest.fn(),
}));

jest.mock('@/api/salesReport', () => ({
  generateReport: jest.fn()
}));

const getBusinessMock = castMock(getBusiness);
const generateReportMock = castMock(generateReport);

const $route = {
  params: {
    id: 1
  }
}

const user : User = {
  id: 1,
  firstName: 'Joe',
  lastName: 'Bloggs',
  middleName: 'ahh',
  nickname: 'Doe',
  bio: 'Bio',
  email: 'abc@123.com',
  dateOfBirth: '02-02-1943',
  phoneNumber: '',
  homeAddress: { country: "Spain" },
  created: undefined,
  role: "globalApplicationAdmin",
  businessesAdministered: [],
  images: []
};

const business : Business = {
  id: 1,
  name: "Some Business Name",
  address: {
    "country": "Some Country",
    "streetName": "Some Street Name",
    "streetNumber": "1",
    "city": "Some City",
    "district": "Some District",
    "postcode": "1234",
    "region": "Some Region"
  },
  businessType: "Retail Trade",
  description: "Some Description",
  created: "date",
  images: [{ id: 1, filename: 'coolImage.jpg', thumbnailFilename: "blah" }],
  administrators: [
    user
  ],
  rank: {
    name: "bronze",
    threshold: 1
  },
  primaryAdministratorId: 0,
  points: 0
}

function generateSaleRecord(isAllColumns: boolean) {
  let saleRecord: SaleRecord = {
    startDate: "start",
    endDate: "end",
    uniqueListingsSold: 1,
    uniqueBuyers: 2,
    uniqueProducts: 3,
    totalQuantitySold: 4,
    totalPriceSold: 5
  }

  if (isAllColumns) {
    saleRecord["averageLikeCount"] = 6;
    saleRecord["averageDaysToSell"] = 7;
  }

  return [saleRecord];
}

describe('SaleResult.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    getBusinessMock.mockResolvedValueOnce(business);

    wrapper = mount(SalesReportPage, {
      mocks: {
        $route
      },
      localVue,
      vuetify,
      stubs: {
        'ReportOptionsBar': ReportOptionsBar,
        'SalesReportTable': SalesReportTable
      },
    });
  });

  afterEach(() => {
    wrapper.destroy();
    jest.resetAllMocks();
  });

  it("Must contain the page title of the format: Sales Report - *businessName*", async() => {
    expect(wrapper.text()).toContain(`Sales Report - Some Business Name`);
  });

  it("Updates errorMessage and defaults fullReport to undefined, if the api endpoint generateReport has failed.", async() => {
    instance.get.mockRejectedValueOnce({response: { status: 99 }});
    await wrapper.vm.generateFullReport({businessId: 1, fromDate: "start", toDate: "end", granularity: "granularity"});
    expect(wrapper.vm.errorMessage).toEqual('Request failed: 99');
  });
})