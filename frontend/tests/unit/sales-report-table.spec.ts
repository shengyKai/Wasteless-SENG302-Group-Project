import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SalesReportTable from "@/components/BusinessProfile/SalesReport/SalesReportTable.vue";

Vue.use(Vuetify);

let dailyHeaders = ['Year', "Day in Month", "Week No.", "Month"];
let weeklyHeaders = ['Year', "Week No.", "Month"];
let monthlyHeaders = ['Year', "Month"];
let yearlyHeaders = ['Year'];
let baseHeaders = [
  'No. of Unique Buyers',
  'No. of Unique Products',
  'Average Time to Sell (days)',
  'Average Like Count',
  'Total Value of all Purchases ($)',
];

describe('SalesReportTable.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    wrapper = mount(SalesReportTable, {
      localVue,
      vuetify,
      propsData: {
        fullReport: {
          reportData: [
            {
              somedata : 'blah'
            }
          ],
          reportType: "daily"
        }
      },
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("If the reportType is daily, the appropriate table headers are shown", () => {
    for (const header of dailyHeaders.concat(baseHeaders)) {
      expect(wrapper.text()).toContain(header);
    }
  });

  it("If the reportType is weekly, the appropriate table headers are shown", async () => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "weekly"
    });
    for (const header of weeklyHeaders.concat(baseHeaders)) {
      expect(wrapper.text()).toContain(header);
    }
  });

  it("If the reportType is monthly, the appropriate table headers are shown", async () => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "monthly"
    });
    for (const header of monthlyHeaders.concat(baseHeaders)) {
      expect(wrapper.text()).toContain(header);
    }
  });

  it("If the reportType is yearly, the appropriate table headers are shown", async () => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "yearly"
    });
    for (const header of yearlyHeaders.concat(baseHeaders)) {
      expect(wrapper.text()).toContain(header);
    }
  });

  it("If the reportType is none, the appropriate table headers are shown", async () => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "none"
    });
    for (const header of baseHeaders) {
      expect(wrapper.text()).toContain(header);
    }
  });
});