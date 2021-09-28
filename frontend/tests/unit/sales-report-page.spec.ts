import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import SaleReportPage from "@/components/BusinessProfile/SalesReport/SalesReportPage.vue";

Vue.use(Vuetify);

describe('SaleResult.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    wrapper = mount(SaleReportPage, {
      localVue,
      vuetify,
      stubs: [
        'ReportOptionsBar',
        'SalesReportTable'
      ],
      data() {
        return {
          businessName: "Some biz",
        };
      }
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("Must contain the page title of the format: Sales Report - *businessName*", () => {
    expect(wrapper.text()).toContain(`Sales Report - Some biz`);
  })
})