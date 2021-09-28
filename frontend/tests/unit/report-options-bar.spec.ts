import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import ReportOptionsBar from "@/components/BusinessProfile/SalesReport/ReportOptionsBar.vue";

Vue.use(Vuetify);

describe('ReportOptionsBar.vue', () => {
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

  it("The maximum possible date for the From and To datepickers are at most the present day or toDate", ()=> {
    expect(new Date(wrapper.vm.maxFromDate) <= new Date()).toBeTruthy();
    expect(new Date(wrapper.vm.maxFromDate) <= new Date(wrapper.vm.toDate)).toBeTruthy();
    expect(new Date(wrapper.vm.maxToDate) <= new Date()).toBeTruthy();
  });

  it("If toDate is not set, maxFromDate will be changed to the present day", async() => {
    await wrapper.setData({
      toDate: null
    });
    expect(wrapper.vm.maxFromDate).toEqual(new Date().toISOString().slice(0, 10));
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
  });

  it("If toDate is set, periodBefore must be null", async() => {
    await wrapper.setData({
      toDate: new Date("01-12-2020").toISOString().slice(0, 10)
    });
    expect(wrapper.vm.periodBefore).toEqual(null);
  });

  it("If toDate is set, periodBefore must be null", async() => {
    await wrapper.setData({
      fromDate: new Date("01-12-2020").toISOString().slice(0, 10)
    });
    expect(wrapper.vm.periodBefore).toEqual(null);
  });

  it("If periodBefore is set, toDate and fromDate must be null", async() => {
    await wrapper.setData({
      periodBefore: "something"
    });
    expect(wrapper.vm.toDate).toEqual(null);
    expect(wrapper.vm.fromDate).toEqual(null);
  });

  it("If toDate is set and fromDate is null, fromDate will be set to toDate's value", async() => {
    // Have to separate the setData for both of it because the watcher will not be triggered if both is set 
    // at the same time
    await wrapper.setData({
      fromDate: null
    });
    await wrapper.setData({
      toDate: new Date().toISOString().slice(0, 10)
    });
    expect(wrapper.vm.fromDate).toEqual(new Date().toISOString().slice(0, 10));
  });

  it("If fromDate is set and toDate is null, toDate will be set to fromDate's value", async() => {
    // Have to separate the setData for both of it because the watcher will not be triggered if both is set 
    // at the same time
    await wrapper.setData({
      toDate: null,
    });
    await wrapper.setData({
      fromDate: new Date().toISOString().slice(0, 10)
    });
    expect(wrapper.vm.toDate).toEqual(new Date().toISOString().slice(0, 10));
  });
});