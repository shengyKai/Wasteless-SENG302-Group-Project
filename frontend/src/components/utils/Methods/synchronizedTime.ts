import vue from 'vue';

/**
 * Object which allows current time to be updated at the same rate for all components.
 */
let synchronizedTime = vue.observable({now: new Date()});
setInterval(() => synchronizedTime.now = new Date(), 1000);

/**
  * Other components should not be able to modify the synchronized time object.
  */
Object.freeze(synchronizedTime);

export default synchronizedTime;