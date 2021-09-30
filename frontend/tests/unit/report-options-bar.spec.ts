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
          fromDate: new Date("2012-01-01").toISOString().slice(0, 10),
          toDate: new Date("2012-02-01").toISOString().slice(0, 10),
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

  it('If preset period "One year before" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'One year before',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(new Date("2011-12-21"));
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it('If preset period "One month before" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'One month before',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(new Date("2012-11-21"));
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it('If preset period "One day before" is selected, then the fromDate/toDate should be set accordingly', async () => {
    await wrapper.setData({
      presetPeriodUserString: 'One day before',
    });

    expect(new Date(wrapper.vm.fromDate)).toStrictEqual(currentDate);
    expect(new Date(wrapper.vm.toDate)).toStrictEqual(currentDate);
  });

  it("Clicking on generate will emit an event to SalesReportPage along with the specified report options", async () => {
    await findButton("Generate").trigger("click");
    expect(wrapper.emitted().sendRequestParams).toBeTruthy();
    expect(wrapper.emitted().sendRequestParams as any[1]).toEqual([[{
      fromDate: new Date("2012-01-01").toISOString().slice(0, 10),
      toDate: new Date("2012-02-01").toISOString().slice(0, 10),
      granularity: "yearly"
    }]]);
  });

  it('Matches snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });
});