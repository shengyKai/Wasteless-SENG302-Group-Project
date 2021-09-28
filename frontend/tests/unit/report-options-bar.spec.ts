import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import ReportOptionsBar from "@/components/BusinessProfile/SalesReport/ReportOptionsBar.vue";

Vue.use(Vuetify);

describe('SaleResult.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;

  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    wrapper = mount(ReportOptionsBar, {
      localVue,
      vuetify,
      data() {
        return {
          fromDate: new Date("01-01-2021").toISOString().slice(0, 10),
          toDate: new Date("01-01-2021").toISOString().slice(0, 10),
        };
      }
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("The maximum possible date for the From and To datepickers are at most the present day", ()=> {
    expect(new Date(wrapper.vm.maxFromDate) < new Date()).toBeTruthy();
    expect(new Date(wrapper.vm.maxToDate) < new Date()).toBeTruthy();
  });

  it("If the toDate date is before the fromDate date, the fromDate value is changed to that value", async ()=> {
    await wrapper.setData({
      toDate: new Date("01-12-2020").toISOString().slice(0, 10)
    });
    expect(wrapper.vm.fromDate).toEqual(wrapper.vm.toDate);
  });

  it("If the fromDate date is after the toDate date, the toDate value is changed to that value", async ()=> {
    await wrapper.setData({
      fromDate: new Date().toISOString().slice(0, 10)
    });
    expect(wrapper.vm.toDate).toEqual(wrapper.vm.fromDate);
  })
});