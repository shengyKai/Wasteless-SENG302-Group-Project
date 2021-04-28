export default {
  methods: {
    //method to return the appropriate format for addresses depending on the fields provided
    convertAddressToReadableText: function(address: any, status: string) {
      //full means the address format will show all fields, partial means the format will show some location fields only
      if (status === "full") {
        if (address.district !== ""){
          return `${address.streetNumber} ${address.streetName}, ${address.district}\n` +
                  `${address.city} ${address.postcode}\n ${address.region}, ${address.country}`;
        }
        return `${address.streetNumber} ${address.streetName}\n` +
                `${address.city} ${address.postcode}\n${address.region}, ${address.country}`;
      } else {
        return `${address.city}, ${address.region}, ${address.country}`;
      }
    }
  }
};