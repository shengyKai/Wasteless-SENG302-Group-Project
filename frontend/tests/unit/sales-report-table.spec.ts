import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SalesReportTable from "@/components/BusinessProfile/SalesReport/SalesReportTable.vue";

Vue.use(Vuetify);

let dailyHeaders = ["Day of the Month", "Week No.", "Month"];
let weeklyHeaders = ["Week No.", "Month"];
let monthlyHeaders = ["Month"];
let baseHeaders = ['Year', 'No. of Unique Buyers','No. of Unique Products', 'Average Time to Sell (days)', 'Average Like Count', 
                      'Total Value of all Purchases ($)']
let titles = ["daily", "weekly", "monthly", "yearly"];

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

  it.each(titles)("Shows the correct report title based on the reportType", async (title) => {
    await wrapper.setProps({
      reportType: title
    });
    console.log(wrapper.vm.reportTitle);
    expect(wrapper.text()).toContain(`${title.charAt(0).toUpperCase() + title.slice(1)} Report`);
  });

  it.each(dailyHeaders.concat(baseHeaders))("If the reportType is daily, the appropriate table headers are shown", (header) => {
    expect(wrapper.text()).toContain(header);
  });

  it.each(weeklyHeaders.concat(baseHeaders))("If the reportType is weekly, the appropriate table headers are shown", async (header) => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "weekly"
    });
    expect(wrapper.text()).toContain(header);
  });

  it.each(monthlyHeaders.concat(baseHeaders))("If the reportType is monthly, the appropriate table headers are shown", async (header) => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "monthly"
    });
    expect(wrapper.text()).toContain(header);
  });

  it.each(baseHeaders)("If the reportType is yearly, the appropriate table headers are shown", async (header) => {
    await wrapper.setProps({
      reportData: [
        {
          somedata : 'blah'
        }
      ],
      reportType: "yearly"
    });
    expect(wrapper.text()).toContain(header);
  });
});