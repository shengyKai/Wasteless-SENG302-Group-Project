const EMITTER_URL = process.env.VUE_APP_SERVER_ADD + '/emitter';

let eventSource: EventSource;
let lastErrorTime = Number.MIN_VALUE;

export function initialiseEventSourceForUser(userId: number): void {
  eventSource?.close();

  eventSource = new EventSource(EMITTER_URL + "?userId=" + encodeURIComponent(userId), {
    withCredentials: true,
  });
  eventSource.addEventListener("error", (event) => {
    if (eventSource.readyState === EventSource.CLOSED) {
      return;
    }

    let errorTime = Date.now();
    if (lastErrorTime + 10 > errorTime) { // Been less than 10s since last error
      console.error("Error occured", event);
      eventSource.close(); // Give up
    }
  });
}

export function addEventMessageHandler(handler: (event: MessageEvent) => void): void {
  eventSource.addEventListener('newsfeed' as any, handler);
}