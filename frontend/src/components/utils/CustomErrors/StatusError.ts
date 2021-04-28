export default class StatusError extends Error {
  constructor(message : string) {
    super(message); // (1)
    this.name = "StatusError"; // (2)
  }
}