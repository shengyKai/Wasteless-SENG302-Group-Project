import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import ReportOptionsBar from "@/components/BusinessProfile/SalesReport/ReportOptionsBar.vue";
import {findButtonWithText} from "./utils";

Vue.use(Vuetify);

const currentDate = new Date("2012-12-21");
describe('ReportOptionsBar.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;


  function findButton(component: string) {
    return findButtonWithText(wrapper, component);
  }

  beforeAll(() => {
    jest
      .useFakeTimers('modern')
      .setSystemTime(currentDate);
  });


  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    wrapper = mount(ReportOptionsBar, {
      localVue,
      vuetify,
      data() {
        return {
          fromDate: "2012-01-01",
          toDate: "2012-02-01",
          granularity: "yearly",
        };
      }
    });
  });

  afterEach(() => {
    wrapper.destroy();
  });

  it("The maximum/minimum possible date for the From and To datepickers are correct", ()=> {
    expect(new Date(wrapper.vm.maxFromDate)).toStrictEqual(new Date("2012-02-01")); // Maximum fromDate is toDate
    expect(new Date(wrapper.vm.maxToDate)).toStrictEqual(currentDate); // Maximum toDate is now
    expect(new Date(wrapper.vm.minToDate)).toStrictEqual(new Date("2012-01-01")); // Minimum toDate is fromDate
  });

  it("If toDate is not set, maxFromDate will be the present day", async() => {
    await wrapper.setData({
      toDate: undefined,
    });
    expect(new Date(wrapper.vm.maxFromDate)).toStrictEqual(currentDate);
  });

  it('If preset period "Current Year" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'Current Year',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(new Date("2012-01-01"));
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it('If preset period "Current Month" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'Current Month',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(new Date("2012-12-01"));
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it('If preset period "Current Week" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'Current Week',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(new Date("2012-12-17"));
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it('If preset period "Today" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'Today',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(currentDate);
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it("Clicking on generate will emit an event to SalesReportPage along with the specified report options", async () => {
    await findButton("Generate").trigger("click");
    expect(wrapper.emitted().sendRequestParams).toBeTruthy();
    expect(wrapper.emitted().sendRequestParams as any[1]).toEqual([[{
      fromDate: "2012-01-01",
      toDate: "2012-02-01",
      granularity: "yearly"
    }]]);
  });

  it('Matches snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });
});