
import Vue from 'vue';
import Vuetify from 'vuetify';
import { createLocalVue, mount, Wrapper } from '@vue/test-utils';
import KeywordCreated from '@/components/home/newsfeed/KeywordCreated.vue';

Vue.use(Vuetify);

describe('KeywordCreated.vue', () => {
  let wrapper: Wrapper<any>;
  let vuetify: Vuetify;
  beforeEach(async () => {
    const localVue = createLocalVue();
    vuetify = new Vuetify();

    const app = document.createElement ("div");
    app.setAttribute ("data-app", "true");
    document.body.append (app);

    wrapper = mount(KeywordCreated, {
      localVue,
      vuetify,
      propsData: {
        event: {
          id: 101,
          created: "2021-01-01T12:00:00Z",
          type: "KeywordCreatedEvent",
          keyword: {
            name: 'TestKeywordName',
          },
          creator: {firstName: 'John'  , lastName: 'Smith'},
        },
      }
    });
  });

  it("Title is correct", () => {
    let eventWrapper = wrapper.findComponent({ name: 'Event'});
    expect(eventWrapper.props().title).toBe('Keyword "TestKeywordName" has been created');
  });

  it('Contains creator\'s name', () => {
    expect(wrapper.text()).toContain('John Smith');
  });

  it('Must match snapshot', () => {
    expect(wrapper).toMatchSnapshot();
  });
});