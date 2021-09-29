import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SaleReportPage from "@/components/BusinessProfile/SalesReport/SalesReportPage.vue";
import {getBusiness, Business} from "@/api/business";
import { User } from '@/api/user';
import {castMock} from './utils';

Vue.use(Vuetify);

jest.mock('@/api/business', () => ({
  getBusiness: jest.fn(),
}));

const getBusinessMock = castMock(getBusiness);

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
  })
})