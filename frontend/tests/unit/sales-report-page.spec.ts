import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SaleReportPage from "@/components/BusinessProfile/SalesReport/SalesReportPage.vue";
import {getBusiness, Business} from "@/api/business";
import {generateReport} from "@/api/salesReport";
import { User } from '@/api/user';
import {castMock, flushQueue} from './utils';

Vue.use(Vuetify);

jest.mock('@/api/business', () => ({
  getBusiness: jest.fn(),
}));

jest.mock('@/api/salesReport', () => ({
  generateReport: jest.fn(),
}));

const getBusinessMock = castMock(getBusiness);
const generateReportMock = castMock(generateReport);

const $route = {
  params: {
    id: 7
  }
};

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
  id: 7,
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
};

describe('SaleResult.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    getBusinessMock.mockResolvedValueOnce(business);
    wrapper = mount(SaleReportPage, {
      mocks: {
        $route
      },
      localVue,
      vuetify,
      stubs: [
        'ReportOptionsBar',
        'SalesReportTable'
      ],
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("Must contain the page title of the format: Sales Report - *businessName*", async() => {
    expect(wrapper.text()).toContain(`Sales Report - Some Business Name`);
  });

  it('Must contain the ReportOptionsBar', () => {
    expect(wrapper.findComponent({name: 'ReportOptionsBar'}).exists()).toBeTruthy();
  });

  it('If there is no report then SalesReportTable should not be shown', () => {
    expect(wrapper.findComponent({name: 'SalesReportTable'}).exists()).toBeFalsy();
  });

  it('When generate is triggered from ReportOptionsBar, the report should be generated and shown', async () => {
    const fullReport = [{
      startDate: '2021-01-01',
      endDate: '2021-01-01',
      uniqueListingsSold: 0,
      uniqueBuyers: 0,
      uniqueProducts: 0,
      totalPriceSold: 0,
      totalQuantitySold: 0,
    }];
    generateReportMock.mockResolvedValueOnce(fullReport);

    const optionsBar = wrapper.findComponent({name: 'ReportOptionsBar'});
    optionsBar.vm.$emit('sendRequestParams', {fromDate: '2021-01-01', toDate: '2021-01-02', granularity: 'yearly'});

    await flushQueue();

    expect(generateReportMock).toBeCalledWith(7, '2021-01-01', '2021-01-02', 'yearly');

    const table = wrapper.findComponent({name: 'SalesReportTable'});
    expect(table.exists()).toBeTruthy();
    expect(table.props().fullReport).toStrictEqual({reportData: fullReport, reportType: 'yearly'});
  });

  it('When generate is triggered and an error occurs, then the error message should be shown', async () => {
    generateReportMock.mockResolvedValueOnce('test_error_message');

    const optionsBar = wrapper.findComponent({name: 'ReportOptionsBar'});
    optionsBar.vm.$emit('sendRequestParams', {fromDate: '2021-01-01', toDate: '2021-01-02', granularity: 'yearly'});

    await flushQueue();

    expect(wrapper.text()).toContain('test_error_message');
    expect(wrapper.findComponent({name: 'SalesReportTable'}).exists()).toBeFalsy();
  });
});